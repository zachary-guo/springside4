package org.springside.modules.persistence;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springside.modules.utils.Collections3;

import com.google.common.collect.Lists;

public class DynamicSpecifications {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(DynamicSpecifications.class);

	public static <T> Specification<T> bySearchFilter(final Collection<SearchFilter> filters, final Class<T> clazz) {
		return new Specification<T>() {
			@Override
			public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				if (Collections3.isNotEmpty(filters)) {

					List<Predicate> predicates = Lists.newArrayList();
					for (SearchFilter filter : filters) {
						// nested path translate, 如Task的名为"user.name"的filedName, 转换为Task.user.name属性
						String[] names = StringUtils.split(filter.fieldName, ".");
						Path expression = root.get(names[0]);
						for (int i = 1; i < names.length; i++) {
							expression = expression.get(names[i]);
						}

						// 以下代码用于将扁平的字符串转为特定的数据类型，如枚举，日期等
						Object value = filter.value;
						Class expClazz = expression.getJavaType();
						if (expClazz.isEnum()) {
							// 如果是枚举类型，只可能使用 EQ 操作符
							value = Enum.valueOf(expClazz, value.toString());
						} else if (expClazz == Date.class) {
							SimpleDateFormat[] sdfs = new SimpleDateFormat[] {
								new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S"),
								new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),
								new SimpleDateFormat("yyyy-MM-dd HH:mm"),
								new SimpleDateFormat("yyyy-MM-dd"),
							};
							
							for (SimpleDateFormat sdf : sdfs) {
								try {
									value = sdf.parse(value.toString());
									break;
								} catch (ParseException e) {
									logger.error(e.getMessage(), e);
								}
							}
							
						}
						// logic operator
						switch (filter.operator) {
						case EQ:
							predicates.add(builder.equal(expression, value));
							break;
						case LIKE:
							predicates.add(builder.like(expression, "%" + value + "%"));
							break;
						case GT:
							predicates.add(builder.greaterThan(expression, (Comparable) value));
							break;
						case LT:
							predicates.add(builder.lessThan(expression, (Comparable) value));
							break;
						case GTE:
							predicates.add(builder.greaterThanOrEqualTo(expression, (Comparable) value));
							break;
						case LTE:
							predicates.add(builder.lessThanOrEqualTo(expression, (Comparable) value));
							break;
						case IN:
							In<Object> in = builder.in(expression);
							for (Iterator it = ((Iterable) value).iterator(); it.hasNext();) {
								in.value(it.next());
							}
							predicates.add(in);
							break;
						}
						
					}

					// 将所有条件用 and 联合起来
					if (predicates.size() > 0) {
						return builder.and(predicates.toArray(new Predicate[predicates.size()]));
					}
				}

				return builder.conjunction();
			}
		};
	}
}
