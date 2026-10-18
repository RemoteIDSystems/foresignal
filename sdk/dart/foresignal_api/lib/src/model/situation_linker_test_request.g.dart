// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'situation_linker_test_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SituationLinkerTestRequestCWProxy {
  SituationLinkerTestRequest fromSeq(int fromSeq);

  SituationLinkerTestRequest toSeq(int toSeq);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationLinkerTestRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationLinkerTestRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  SituationLinkerTestRequest call({int fromSeq, int toSeq});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSituationLinkerTestRequest.copyWith(...)` or call `instanceOfSituationLinkerTestRequest.copyWith.fieldName(value)` for a single field.
class _$SituationLinkerTestRequestCWProxyImpl
    implements _$SituationLinkerTestRequestCWProxy {
  const _$SituationLinkerTestRequestCWProxyImpl(this._value);

  final SituationLinkerTestRequest _value;

  @override
  SituationLinkerTestRequest fromSeq(int fromSeq) => call(fromSeq: fromSeq);

  @override
  SituationLinkerTestRequest toSeq(int toSeq) => call(toSeq: toSeq);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationLinkerTestRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationLinkerTestRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SituationLinkerTestRequest call({
    Object? fromSeq = const $CopyWithPlaceholder(),
    Object? toSeq = const $CopyWithPlaceholder(),
  }) {
    return SituationLinkerTestRequest(
      fromSeq: fromSeq == const $CopyWithPlaceholder() || fromSeq == null
          ? _value.fromSeq
          // ignore: cast_nullable_to_non_nullable
          : fromSeq as int,
      toSeq: toSeq == const $CopyWithPlaceholder() || toSeq == null
          ? _value.toSeq
          // ignore: cast_nullable_to_non_nullable
          : toSeq as int,
    );
  }
}

extension $SituationLinkerTestRequestCopyWith on SituationLinkerTestRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSituationLinkerTestRequest.copyWith(...)` or `instanceOfSituationLinkerTestRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SituationLinkerTestRequestCWProxy get copyWith =>
      _$SituationLinkerTestRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SituationLinkerTestRequest _$SituationLinkerTestRequestFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SituationLinkerTestRequest', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['fromSeq', 'toSeq']);
  final val = SituationLinkerTestRequest(
    fromSeq: $checkedConvert('fromSeq', (v) => (v as num).toInt()),
    toSeq: $checkedConvert('toSeq', (v) => (v as num).toInt()),
  );
  return val;
});

Map<String, dynamic> _$SituationLinkerTestRequestToJson(
  SituationLinkerTestRequest instance,
) => <String, dynamic>{'fromSeq': instance.fromSeq, 'toSeq': instance.toSeq};
