// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'situation_linker_test_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SituationLinkerTestResultCWProxy {
  SituationLinkerTestResult fromSeq(int fromSeq);

  SituationLinkerTestResult toSeq(int toSeq);

  SituationLinkerTestResult linkerVersion(String linkerVersion);

  SituationLinkerTestResult inputsConsidered(int? inputsConsidered);

  SituationLinkerTestResult decisions(List<SituationLinkDecision> decisions);

  SituationLinkerTestResult truncated(bool? truncated);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationLinkerTestResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationLinkerTestResult(...).copyWith(id: 12, name: "My name")
  /// ```
  SituationLinkerTestResult call({
    int fromSeq,
    int toSeq,
    String linkerVersion,
    int? inputsConsidered,
    List<SituationLinkDecision> decisions,
    bool? truncated,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSituationLinkerTestResult.copyWith(...)` or call `instanceOfSituationLinkerTestResult.copyWith.fieldName(value)` for a single field.
class _$SituationLinkerTestResultCWProxyImpl
    implements _$SituationLinkerTestResultCWProxy {
  const _$SituationLinkerTestResultCWProxyImpl(this._value);

  final SituationLinkerTestResult _value;

  @override
  SituationLinkerTestResult fromSeq(int fromSeq) => call(fromSeq: fromSeq);

  @override
  SituationLinkerTestResult toSeq(int toSeq) => call(toSeq: toSeq);

  @override
  SituationLinkerTestResult linkerVersion(String linkerVersion) =>
      call(linkerVersion: linkerVersion);

  @override
  SituationLinkerTestResult inputsConsidered(int? inputsConsidered) =>
      call(inputsConsidered: inputsConsidered);

  @override
  SituationLinkerTestResult decisions(List<SituationLinkDecision> decisions) =>
      call(decisions: decisions);

  @override
  SituationLinkerTestResult truncated(bool? truncated) =>
      call(truncated: truncated);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationLinkerTestResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationLinkerTestResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SituationLinkerTestResult call({
    Object? fromSeq = const $CopyWithPlaceholder(),
    Object? toSeq = const $CopyWithPlaceholder(),
    Object? linkerVersion = const $CopyWithPlaceholder(),
    Object? inputsConsidered = const $CopyWithPlaceholder(),
    Object? decisions = const $CopyWithPlaceholder(),
    Object? truncated = const $CopyWithPlaceholder(),
  }) {
    return SituationLinkerTestResult(
      fromSeq: fromSeq == const $CopyWithPlaceholder() || fromSeq == null
          ? _value.fromSeq
          // ignore: cast_nullable_to_non_nullable
          : fromSeq as int,
      toSeq: toSeq == const $CopyWithPlaceholder() || toSeq == null
          ? _value.toSeq
          // ignore: cast_nullable_to_non_nullable
          : toSeq as int,
      linkerVersion:
          linkerVersion == const $CopyWithPlaceholder() || linkerVersion == null
          ? _value.linkerVersion
          // ignore: cast_nullable_to_non_nullable
          : linkerVersion as String,
      inputsConsidered: inputsConsidered == const $CopyWithPlaceholder()
          ? _value.inputsConsidered
          // ignore: cast_nullable_to_non_nullable
          : inputsConsidered as int?,
      decisions: decisions == const $CopyWithPlaceholder() || decisions == null
          ? _value.decisions
          // ignore: cast_nullable_to_non_nullable
          : decisions as List<SituationLinkDecision>,
      truncated: truncated == const $CopyWithPlaceholder()
          ? _value.truncated
          // ignore: cast_nullable_to_non_nullable
          : truncated as bool?,
    );
  }
}

extension $SituationLinkerTestResultCopyWith on SituationLinkerTestResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSituationLinkerTestResult.copyWith(...)` or `instanceOfSituationLinkerTestResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SituationLinkerTestResultCWProxy get copyWith =>
      _$SituationLinkerTestResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SituationLinkerTestResult _$SituationLinkerTestResultFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SituationLinkerTestResult', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['fromSeq', 'toSeq', 'linkerVersion', 'decisions'],
  );
  final val = SituationLinkerTestResult(
    fromSeq: $checkedConvert('fromSeq', (v) => (v as num).toInt()),
    toSeq: $checkedConvert('toSeq', (v) => (v as num).toInt()),
    linkerVersion: $checkedConvert('linkerVersion', (v) => v as String),
    inputsConsidered: $checkedConvert(
      'inputsConsidered',
      (v) => (v as num?)?.toInt(),
    ),
    decisions: $checkedConvert(
      'decisions',
      (v) => (v as List<dynamic>)
          .map((e) => SituationLinkDecision.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    truncated: $checkedConvert('truncated', (v) => v as bool?),
  );
  return val;
});

Map<String, dynamic> _$SituationLinkerTestResultToJson(
  SituationLinkerTestResult instance,
) => <String, dynamic>{
  'fromSeq': instance.fromSeq,
  'toSeq': instance.toSeq,
  'linkerVersion': instance.linkerVersion,
  'inputsConsidered': ?instance.inputsConsidered,
  'decisions': instance.decisions.map((e) => e.toJson()).toList(),
  'truncated': ?instance.truncated,
};
