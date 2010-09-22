package choco.cp.solver.constraints;

import choco.cp.solver.SettingType;
import choco.kernel.solver.SolverException;

import java.util.List;

import static choco.cp.solver.SettingType.*;

public final class BitFlags {


	private long flags;

	
	public BitFlags() {
		super();
		flags = 0;
	}
	
	/**
	 * copy constructor.
	 */
	public BitFlags(BitFlags toCopy) {
		super();
		this.flags = toCopy.flags;
	}

	public long getValue() {
		return flags; 
	}

	public void clear() {
		flags = 0;
	}
	
	public boolean isEmpty() {
		return flags == 0;
	}

	public boolean contains(final long mask) {
		return (flags & mask) == mask;
	}
	
	public boolean or(SettingType... settings) {
		for (SettingType setting : settings) {
			if(contains(setting)) {
				return true;
			}
		}
		return false;
	}

	public boolean contains(SettingType setting) {
		return contains(setting.getBitMask());
	}

	public void set(final long mask) {
		flags |= mask;		
	}

	public void toggle(final long mask) {
		flags ^= mask;
	}

	public void unset(final long mask) {
		flags &= ~mask;
	}

	public void read(List<String> options, SettingType... settings) {
		for (SettingType setting : settings) {
			if(options.contains(setting.getOptionName())) {
				set(setting);
			}
		}
	}
	public void set(BitFlags f) {
		this.set(f.getValue());
	}
	public void unset(SettingType... settings) {
		for (SettingType setting : settings) {
			unset(setting.getBitMask());
		}
	}
	
	public void set(SettingType... settings) {
		for (SettingType setting : settings) {
			set(setting.getBitMask());
		}
	}

	public String toSettingsString() {
		StringBuilder b = new StringBuilder();
		for(SettingType setting : SettingType.values()) {
			if(contains(setting)) {
				b.append(setting.getName()).append(" ");
			}
		}
		if(b.length()>0) {b.deleteCharAt(b.length()-1);}
		return new String(b);
	}

	public String toSettingsLabels() {
		StringBuilder b = new StringBuilder();
		for(SettingType setting : SettingType.values()) {
			if(contains(setting)) {
				b.append(setting.getLabel()).append("-");
			}
		}
		if(b.length()>0) {b.deleteCharAt(b.length()-1);}
		return new String(b);
	}

	@Override
	public String toString() {
		return String.valueOf(flags);
	}

	public void readPackOptions(final List<String> options) {
		read(options, ADDITIONAL_RULES, DYNAMIC_LB, FILL_BIN, LAST_BINS_EMPTY);
	}
	
	public void readCumulativeOptions(final List<String> options) {
		read(options, TASK_INTERVAL, TASK_INTERVAL_SLOW, VHM_CEF_ALGO_N2K, VILIM_CEF_ALGO);
	}


	public void readDisjunctiveOptions(final List<String> options) {
		read(options, OVERLOAD_CHECKING, NF_NL, DETECTABLE_PRECEDENCE, EDGE_FINDING_D);
		if(isEmpty()) set(NF_NL, DETECTABLE_PRECEDENCE, EDGE_FINDING_D);
		if(options.contains(VILIM_FILTERING.getOptionName())) {
			set(VILIM_FILTERING);
		} else {
			set(DEFAULT_FILTERING);
		}
		if(options.contains(SINGLE_RULE_FILTERING.getOptionName())) {
			throw new SolverException("invalid disjunctive setting:" + SINGLE_RULE_FILTERING.getName());
		}
	}
}
