// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'intel_event_member_join_reason.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IntelEventMemberJoinReasonCWProxy {
  IntelEventMemberJoinReason clause(String? clause);

  IntelEventMemberJoinReason distanceKm(num? distanceKm);

  IntelEventMemberJoinReason dtMinutes(num? dtMinutes);

  IntelEventMemberJoinReason matchedKeys(List<String>? matchedKeys);

  IntelEventMemberJoinReason regionTag(String? regionTag);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelEventMemberJoinReason(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelEventMemberJoinReason(...).copyWith(id: 12, name: "My name")
  /// ```
  IntelEventMemberJoinReason call({
    String? clause,
    num? distanceKm,
    num? dtMinutes,
    List<String>? matchedKeys,
    String? regionTag,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIntelEventMemberJoinReason.copyWith(...)` or call `instanceOfIntelEventMemberJoinReason.copyWith.fieldName(value)` for a single field.
class _$IntelEventMemberJoinReasonCWProxyImpl
    implements _$IntelEventMemberJoinReasonCWProxy {
  const _$IntelEventMemberJoinReasonCWProxyImpl(this._value);

  final IntelEventMemberJoinReason _value;

  @override
  IntelEventMemberJoinReason clause(String? clause) => call(clause: clause);

  @override
  IntelEventMemberJoinReason distanceKm(num? distanceKm) =>
      call(distanceKm: distanceKm);

  @override
  IntelEventMemberJoinReason dtMinutes(num? dtMinutes) =>
      call(dtMinutes: dtMinutes);

  @override
  IntelEventMemberJoinReason matchedKeys(List<String>? matchedKeys) =>
      call(matchedKeys: matchedKeys);

  @override
  IntelEventMemberJoinReason regionTag(String? regionTag) =>
      call(regionTag: regionTag);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelEventMemberJoinReason(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelEventMemberJoinReason(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IntelEventMemberJoinReason call({
    Object? clause = const $CopyWithPlaceholder(),
    Object? distanceKm = const $CopyWithPlaceholder(),
    Object? dtMinutes = const $CopyWithPlaceholder(),
    Object? matchedKeys = const $CopyWithPlaceholder(),
    Object? regionTag = const $CopyWithPlaceholder(),
  }) {
    return IntelEventMemberJoinReason(
      clause: clause == const $CopyWithPlaceholder()
          ? _value.clause
          // ignore: cast_nullable_to_non_nullable
          : clause as String?,
      distanceKm: distanceKm == const $CopyWithPlaceholder()
          ? _value.distanceKm
          // ignore: cast_nullable_to_non_nullable
          : distanceKm as num?,
      dtMinutes: dtMinutes == const $CopyWithPlaceholder()
          ? _value.dtMinutes
          // ignore: cast_nullable_to_non_nullable
          : dtMinutes as num?,
      matchedKeys: matchedKeys == const $CopyWithPlaceholder()
          ? _value.matchedKeys
          // ignore: cast_nullable_to_non_nullable
          : matchedKeys as List<String>?,
      regionTag: regionTag == const $CopyWithPlaceholder()
          ? _value.regionTag
          // ignore: cast_nullable_to_non_nullable
          : regionTag as String?,
    );
  }
}

extension $IntelEventMemberJoinReasonCopyWith on IntelEventMemberJoinReason {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIntelEventMemberJoinReason.copyWith(...)` or `instanceOfIntelEventMemberJoinReason.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IntelEventMemberJoinReasonCWProxy get copyWith =>
      _$IntelEventMemberJoinReasonCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IntelEventMemberJoinReason _$IntelEventMemberJoinReasonFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('IntelEventMemberJoinReason', json, ($checkedConvert) {
  final val = IntelEventMemberJoinReason(
    clause: $checkedConvert('clause', (v) => v as String?),
    distanceKm: $checkedConvert('distanceKm', (v) => v as num?),
    dtMinutes: $checkedConvert('dtMinutes', (v) => v as num?),
    matchedKeys: $checkedConvert(
      'matchedKeys',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    regionTag: $checkedConvert('regionTag', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$IntelEventMemberJoinReasonToJson(
  IntelEventMemberJoinReason instance,
) => <String, dynamic>{
  'clause': ?instance.clause,
  'distanceKm': ?instance.distanceKm,
  'dtMinutes': ?instance.dtMinutes,
  'matchedKeys': ?instance.matchedKeys,
  'regionTag': ?instance.regionTag,
};
