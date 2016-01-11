package cn.ac.iscas.oncecloudshare.service.extensions.preview.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import cn.ac.iscas.oncecloudshare.service.model.IdEntity;

@Entity
@Table(name = "ocs_preview", uniqueConstraints = { @UniqueConstraint(columnNames = { "input", "converterType", "converter" }) })
public class Preview extends IdEntity {
	private String input;
	private String output;
	// 输出的类型
	private String outputFormat;
	private String converterType;
	private String converter;

	@Column(nullable = false)
	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	@Column(nullable = false)
	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	@Column(nullable = false)
	public String getOutputFormat() {
		return outputFormat;
	}

	public void setOutputFormat(String outputFormat) {
		this.outputFormat = outputFormat;
	}

	@Column(nullable = false)
	public String getConverterType() {
		return converterType;
	}

	public void setConverterType(String converterType) {
		this.converterType = converterType;
	}

	@Column(nullable = false)
	public String getConverter() {
		return converter;
	}

	public void setConverter(String converter) {
		this.converter = converter;
	}
}
