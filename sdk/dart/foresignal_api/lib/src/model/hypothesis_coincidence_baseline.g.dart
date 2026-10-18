// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'hypothesis_coincidence_baseline.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$HypothesisCoincidenceBaselineCWProxy {
  HypothesisCoincidenceBaseline placeboRate(num placeboRate);

  HypothesisCoincidenceBaseline method(String method);

  HypothesisCoincidenceBaseline eventStudyId(String? eventStudyId);

  HypothesisCoincidenceBaseline nPlacebo(int? nPlacebo);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisCoincidenceBaseline(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisCoincidenceBaseline(...).copyWith(id: 12, name: "My name")
  /// ```
  HypothesisCoincidenceBaseline call({
    num placeboRate,
    String method,
    String? eventStudyId,
    int? nPlacebo,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfHypothesisCoincidenceBaseline.copyWith(...)` or call `instanceOfHypothesisCoincidenceBaseline.copyWith.fieldName(value)` for a single field.
class _$HypothesisCoincidenceBaselineCWProxyImpl
    implements _$HypothesisCoincidenceBaselineCWProxy {
  const _$HypothesisCoincidenceBaselineCWProxyImpl(this._value);

  final HypothesisCoincidenceBaseline _value;

  @override
  HypothesisCoincidenceBaseline placeboRate(num placeboRate) =>
      call(placeboRate: placeboRate);

  @override
  HypothesisCoincidenceBaseline method(String method) => call(method: method);

  @override
  HypothesisCoincidenceBaseline eventStudyId(String? eventStudyId) =>
      call(eventStudyId: eventStudyId);

  @override
  HypothesisCoincidenceBaseline nPlacebo(int? nPlacebo) =>
      call(nPlacebo: nPlacebo);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisCoincidenceBaseline(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisCoincidenceBaseline(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  HypothesisCoincidenceBaseline call({
    Object? placeboRate = const $CopyWithPlaceholder(),
    Object? method = const $CopyWithPlaceholder(),
    Object? eventStudyId = const $CopyWithPlaceholder(),
    Object? nPlacebo = const $CopyWithPlaceholder(),
  }) {
    return HypothesisCoincidenceBaseline(
      placeboRate:
          placeboRate == const $CopyWithPlaceholder() || placeboRate == null
          ? _value.placeboRate
          // ignore: cast_nullable_to_non_nullable
          : placeboRate as num,
      method: method == const $CopyWithPlaceholder() || method == null
          ? _value.method
          // ignore: cast_nullable_to_non_nullable
          : method as String,
      eventStudyId: eventStudyId == const $CopyWithPlaceholder()
          ? _value.eventStudyId
          // ignore: cast_nullable_to_non_nullable
          : eventStudyId as String?,
      nPlacebo: nPlacebo == const $CopyWithPlaceholder()
          ? _value.nPlacebo
          // ignore: cast_nullable_to_non_nullable
          : nPlacebo as int?,
    );
  }
}

extension $HypothesisCoincidenceBaselineCopyWith
    on HypothesisCoincidenceBaseline {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfHypothesisCoincidenceBaseline.copyWith(...)` or `instanceOfHypothesisCoincidenceBaseline.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$HypothesisCoincidenceBaselineCWProxy get copyWith =>
      _$HypothesisCoincidenceBaselineCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HypothesisCoincidenceBaseline _$HypothesisCoincidenceBaselineFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('HypothesisCoincidenceBaseline', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['placeboRate', 'method']);
  final val = HypothesisCoincidenceBaseline(
    placeboRate: $checkedConvert('placeboRate', (v) => v as num),
    method: $checkedConvert('method', (v) => v as String),
    eventStudyId: $checkedConvert('eventStudyId', (v) => v as String?),
    nPlacebo: $checkedConvert('nPlacebo', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$HypothesisCoincidenceBaselineToJson(
  HypothesisCoincidenceBaseline instance,
) => <String, dynamic>{
  'placeboRate': instance.placeboRate,
  'method': instance.method,
  'eventStudyId': ?instance.eventStudyId,
  'nPlacebo': ?instance.nPlacebo,
};
