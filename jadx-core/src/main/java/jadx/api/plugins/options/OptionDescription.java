package jadx.api.plugins.options;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

public interface OptionDescription {

	String name();

	String description();

	/**
	 * Possible values.
	 * Empty if not a limited set
	 */
	List<String> values();

	/**
	 * Default value.
	 * Null if required
	 */
	@Nullable
	String defaultValue();

	default OptionType getType() {
		return OptionType.STRING;
	}

	default Set<OptionFlag> getFlags() {
		return Collections.emptySet();
	}
}
