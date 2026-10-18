// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'intel_board_latest_assessments_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IntelBoardLatestAssessmentsInnerCWProxy {
  IntelBoardLatestAssessmentsInner cadence(
    IntelBoardLatestAssessmentsInnerCadenceEnum cadence,
  );

  IntelBoardLatestAssessmentsInner assessmentId(String assessmentId);

  IntelBoardLatestAssessmentsInner headline(String? headline);

  IntelBoardLatestAssessmentsInner createdAt(DateTime createdAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelBoardLatestAssessmentsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelBoardLatestAssessmentsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  IntelBoardLatestAssessmentsInner call({
    IntelBoardLatestAssessmentsInnerCadenceEnum cadence,
    String assessmentId,
    String? headline,
    DateTime createdAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIntelBoardLatestAssessmentsInner.copyWith(...)` or call `instanceOfIntelBoardLatestAssessmentsInner.copyWith.fieldName(value)` for a single field.
class _$IntelBoardLatestAssessmentsInnerCWProxyImpl
    implements _$IntelBoardLatestAssessmentsInnerCWProxy {
  const _$IntelBoardLatestAssessmentsInnerCWProxyImpl(this._value);

  final IntelBoardLatestAssessmentsInner _value;

  @override
  IntelBoardLatestAssessmentsInner cadence(
    IntelBoardLatestAssessmentsInnerCadenceEnum cadence,
  ) => call(cadence: cadence);

  @override
  IntelBoardLatestAssessmentsInner assessmentId(String assessmentId) =>
      call(assessmentId: assessmentId);

  @override
  IntelBoardLatestAssessmentsInner headline(String? headline) =>
      call(headline: headline);

  @override
  IntelBoardLatestAssessmentsInner createdAt(DateTime createdAt) =>
      call(createdAt: createdAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelBoardLatestAssessmentsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelBoardLatestAssessmentsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IntelBoardLatestAssessmentsInner call({
    Object? cadence = const $CopyWithPlaceholder(),
    Object? assessmentId = const $CopyWithPlaceholder(),
    Object? headline = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
  }) {
    return IntelBoardLatestAssessmentsInner(
      cadence: cadence == const $CopyWithPlaceholder() || cadence == null
          ? _value.cadence
          // ignore: cast_nullable_to_non_nullable
          : cadence as IntelBoardLatestAssessmentsInnerCadenceEnum,
      assessmentId:
          assessmentId == const $CopyWithPlaceholder() || assessmentId == null
          ? _value.assessmentId
          // ignore: cast_nullable_to_non_nullable
          : assessmentId as String,
      headline: headline == const $CopyWithPlaceholder()
          ? _value.headline
          // ignore: cast_nullable_to_non_nullable
          : headline as String?,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
    );
  }
}

extension $IntelBoardLatestAssessmentsInnerCopyWith
    on IntelBoardLatestAssessmentsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIntelBoardLatestAssessmentsInner.copyWith(...)` or `instanceOfIntelBoardLatestAssessmentsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IntelBoardLatestAssessmentsInnerCWProxy get copyWith =>
      _$IntelBoardLatestAssessmentsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IntelBoardLatestAssessmentsInner _$IntelBoardLatestAssessmentsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('IntelBoardLatestAssessmentsInner', json, (
  $checkedConvert,
) {
  $checkKeys(
    json,
    requiredKeys: const ['cadence', 'assessmentId', 'createdAt'],
  );
  final val = IntelBoardLatestAssessmentsInner(
    cadence: $checkedConvert(
      'cadence',
      (v) =>
          $enumDecode(_$IntelBoardLatestAssessmentsInnerCadenceEnumEnumMap, v),
    ),
    assessmentId: $checkedConvert('assessmentId', (v) => v as String),
    headline: $checkedConvert('headline', (v) => v as String?),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
  );
  return val;
});

Map<String, dynamic> _$IntelBoardLatestAssessmentsInnerToJson(
  IntelBoardLatestAssessmentsInner instance,
) => <String, dynamic>{
  'cadence':
      _$IntelBoardLatestAssessmentsInnerCadenceEnumEnumMap[instance.cadence]!,
  'assessmentId': instance.assessmentId,
  'headline': ?instance.headline,
  'createdAt': instance.createdAt.toIso8601String(),
};

const _$IntelBoardLatestAssessmentsInnerCadenceEnumEnumMap = {
  IntelBoardLatestAssessmentsInnerCadenceEnum.daily: 'daily',
  IntelBoardLatestAssessmentsInnerCadenceEnum.weekly: 'weekly',
  IntelBoardLatestAssessmentsInnerCadenceEnum.monthly: 'monthly',
};
