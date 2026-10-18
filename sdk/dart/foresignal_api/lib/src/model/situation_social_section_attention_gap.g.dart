// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'situation_social_section_attention_gap.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SituationSocialSectionAttentionGapCWProxy {
  SituationSocialSectionAttentionGap findingId(String findingId);

  SituationSocialSectionAttentionGap expected(num expected);

  SituationSocialSectionAttentionGap observed(num observed);

  SituationSocialSectionAttentionGap platforms(List<String>? platforms);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationSocialSectionAttentionGap(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationSocialSectionAttentionGap(...).copyWith(id: 12, name: "My name")
  /// ```
  SituationSocialSectionAttentionGap call({
    String findingId,
    num expected,
    num observed,
    List<String>? platforms,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSituationSocialSectionAttentionGap.copyWith(...)` or call `instanceOfSituationSocialSectionAttentionGap.copyWith.fieldName(value)` for a single field.
class _$SituationSocialSectionAttentionGapCWProxyImpl
    implements _$SituationSocialSectionAttentionGapCWProxy {
  const _$SituationSocialSectionAttentionGapCWProxyImpl(this._value);

  final SituationSocialSectionAttentionGap _value;

  @override
  SituationSocialSectionAttentionGap findingId(String findingId) =>
      call(findingId: findingId);

  @override
  SituationSocialSectionAttentionGap expected(num expected) =>
      call(expected: expected);

  @override
  SituationSocialSectionAttentionGap observed(num observed) =>
      call(observed: observed);

  @override
  SituationSocialSectionAttentionGap platforms(List<String>? platforms) =>
      call(platforms: platforms);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationSocialSectionAttentionGap(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationSocialSectionAttentionGap(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SituationSocialSectionAttentionGap call({
    Object? findingId = const $CopyWithPlaceholder(),
    Object? expected = const $CopyWithPlaceholder(),
    Object? observed = const $CopyWithPlaceholder(),
    Object? platforms = const $CopyWithPlaceholder(),
  }) {
    return SituationSocialSectionAttentionGap(
      findingId: findingId == const $CopyWithPlaceholder() || findingId == null
          ? _value.findingId
          // ignore: cast_nullable_to_non_nullable
          : findingId as String,
      expected: expected == const $CopyWithPlaceholder() || expected == null
          ? _value.expected
          // ignore: cast_nullable_to_non_nullable
          : expected as num,
      observed: observed == const $CopyWithPlaceholder() || observed == null
          ? _value.observed
          // ignore: cast_nullable_to_non_nullable
          : observed as num,
      platforms: platforms == const $CopyWithPlaceholder()
          ? _value.platforms
          // ignore: cast_nullable_to_non_nullable
          : platforms as List<String>?,
    );
  }
}

extension $SituationSocialSectionAttentionGapCopyWith
    on SituationSocialSectionAttentionGap {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSituationSocialSectionAttentionGap.copyWith(...)` or `instanceOfSituationSocialSectionAttentionGap.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SituationSocialSectionAttentionGapCWProxy get copyWith =>
      _$SituationSocialSectionAttentionGapCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SituationSocialSectionAttentionGap _$SituationSocialSectionAttentionGapFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SituationSocialSectionAttentionGap', json, (
  $checkedConvert,
) {
  $checkKeys(json, requiredKeys: const ['findingId', 'expected', 'observed']);
  final val = SituationSocialSectionAttentionGap(
    findingId: $checkedConvert('findingId', (v) => v as String),
    expected: $checkedConvert('expected', (v) => v as num),
    observed: $checkedConvert('observed', (v) => v as num),
    platforms: $checkedConvert(
      'platforms',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$SituationSocialSectionAttentionGapToJson(
  SituationSocialSectionAttentionGap instance,
) => <String, dynamic>{
  'findingId': instance.findingId,
  'expected': instance.expected,
  'observed': instance.observed,
  'platforms': ?instance.platforms,
};
