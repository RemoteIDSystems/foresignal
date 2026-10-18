// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'doctor_report_llm_tokens.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DoctorReportLlmTokensCWProxy {
  DoctorReportLlmTokens input(int? input);

  DoctorReportLlmTokens output(int? output);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DoctorReportLlmTokens(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DoctorReportLlmTokens(...).copyWith(id: 12, name: "My name")
  /// ```
  DoctorReportLlmTokens call({int? input, int? output});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDoctorReportLlmTokens.copyWith(...)` or call `instanceOfDoctorReportLlmTokens.copyWith.fieldName(value)` for a single field.
class _$DoctorReportLlmTokensCWProxyImpl
    implements _$DoctorReportLlmTokensCWProxy {
  const _$DoctorReportLlmTokensCWProxyImpl(this._value);

  final DoctorReportLlmTokens _value;

  @override
  DoctorReportLlmTokens input(int? input) => call(input: input);

  @override
  DoctorReportLlmTokens output(int? output) => call(output: output);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DoctorReportLlmTokens(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DoctorReportLlmTokens(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DoctorReportLlmTokens call({
    Object? input = const $CopyWithPlaceholder(),
    Object? output = const $CopyWithPlaceholder(),
  }) {
    return DoctorReportLlmTokens(
      input: input == const $CopyWithPlaceholder()
          ? _value.input
          // ignore: cast_nullable_to_non_nullable
          : input as int?,
      output: output == const $CopyWithPlaceholder()
          ? _value.output
          // ignore: cast_nullable_to_non_nullable
          : output as int?,
    );
  }
}

extension $DoctorReportLlmTokensCopyWith on DoctorReportLlmTokens {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDoctorReportLlmTokens.copyWith(...)` or `instanceOfDoctorReportLlmTokens.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DoctorReportLlmTokensCWProxy get copyWith =>
      _$DoctorReportLlmTokensCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DoctorReportLlmTokens _$DoctorReportLlmTokensFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('DoctorReportLlmTokens', json, ($checkedConvert) {
  final val = DoctorReportLlmTokens(
    input: $checkedConvert('input', (v) => (v as num?)?.toInt()),
    output: $checkedConvert('output', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$DoctorReportLlmTokensToJson(
  DoctorReportLlmTokens instance,
) => <String, dynamic>{'input': ?instance.input, 'output': ?instance.output};
