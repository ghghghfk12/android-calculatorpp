/*
 * Copyright (c) 2009-2011. Created by serso aka se.solovyev.
 * For more information, please, contact se.solovyev@gmail.com
 * or visit http://se.solovyev.org
 */

package org.solovyev.android.calculator;

import jscl.math.function.IConstant;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.solovyev.android.calculator.model.MathEntityBuilder;
import org.solovyev.android.calculator.model.Var;
import org.solovyev.android.calculator.model.Vars;
import org.solovyev.common.JBuilder;
import org.solovyev.common.math.MathEntity;
import org.solovyev.common.math.MathRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * User: serso
 * Date: 9/29/11
 * Time: 4:57 PM
 */
public class CalculatorVarsRegistry extends AbstractCalculatorMathRegistry<IConstant, Var> {

	@Nonnull
	public static final String ANS = "ans";

	@Nonnull
	private static final Map<String, String> substitutes = new HashMap<String, String>();

	static {
		substitutes.put("π", "pi");
		substitutes.put("Π", "PI");
		substitutes.put("∞", "inf");
		substitutes.put("h", "h_reduced");
		substitutes.put("NaN", "nan");
	}

	public CalculatorVarsRegistry(@Nonnull MathRegistry<IConstant> mathRegistry,
								  @Nonnull MathEntityDao<Var> mathEntityDao) {
		super(mathRegistry, "c_var_description_", mathEntityDao);
	}

	public static <T extends MathEntity> void saveVariable(@Nonnull CalculatorMathRegistry<T> registry,
														   @Nonnull MathEntityBuilder<? extends T> builder,
														   @Nullable T editedInstance,
														   @Nonnull Object source, boolean save) {
		final T addedVar = registry.add(builder);

		if (save) {
			registry.save();
		}

		if (editedInstance == null) {
			Locator.getInstance().getCalculator().fireCalculatorEvent(CalculatorEventType.constant_added, addedVar, source);
		} else {
			Locator.getInstance().getCalculator().fireCalculatorEvent(CalculatorEventType.constant_changed, ChangeImpl.newInstance(editedInstance, addedVar), source);
		}
	}

	@Nonnull
	@Override
	protected Map<String, String> getSubstitutes() {
		return substitutes;
	}

	public synchronized void load() {
		super.load();

		tryToAddAuxVar("x");
		tryToAddAuxVar("y");
		tryToAddAuxVar("t");
		tryToAddAuxVar("j");


		/*Log.d(AndroidVarsRegistry.class.getName(), vars.size() + " variables registered!");
		for (Var var : vars) {
			Log.d(AndroidVarsRegistry.class.getName(), var.toString());
		}*/
	}


	@Nonnull
	@Override
	protected JBuilder<? extends IConstant> createBuilder(@Nonnull Var entity) {
		return new Var.Builder(entity);
	}

	@Nonnull
	@Override
	protected MathEntityPersistenceContainer<Var> createPersistenceContainer() {
		return new Vars();
	}

	private void tryToAddAuxVar(@Nonnull String name) {
		if (!contains(name)) {
			add(new Var.Builder(name, (String) null));
		}
	}

	@Nonnull
	@Override
	protected Var transform(@Nonnull IConstant entity) {
		if (entity instanceof Var) {
			return (Var) entity;
		} else {
			return new Var.Builder(entity).create();
		}
	}

	@Override
	public String getDescription(@Nonnull String mathEntityName) {
		final IConstant var = get(mathEntityName);
		if (var != null && !var.isSystem()) {
			return var.getDescription();
		} else {
			return super.getDescription(mathEntityName);
		}
	}

	@Override
	public String getCategory(@Nonnull IConstant var) {
		for (VarCategory category : VarCategory.values()) {
			if (category.isInCategory(var)) {
				return category.name();
			}
		}

		return null;
	}
}