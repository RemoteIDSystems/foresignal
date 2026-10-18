// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'assessment_published_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AssessmentPublishedEventCWProxy {
  AssessmentPublishedEvent assessmentId(String assessmentId);

  AssessmentPublishedEvent cycleId(String? cycleId);

  AssessmentPublishedEvent situationId(String? situationId);

  AssessmentPublishedEvent cadence(AssessmentPublishedEventCadenceEnum cadence);

  AssessmentPublishedEvent status(AssessmentPublishedEventStatusEnum status);

  AssessmentPublishedEvent periodStart(DateTime? periodStart);

  AssessmentPublishedEvent periodEnd(DateTime? periodEnd);

  AssessmentPublishedEvent headline(String? headline);

  AssessmentPublishedEvent private(bool? private);

  AssessmentPublishedEvent at(DateTime at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AssessmentPublishedEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AssessmentPublishedEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  AssessmentPublishedEvent call({
    String assessmentId,
    String? cycleId,
    String? situationId,
    AssessmentPublishedEventCadenceEnum cadence,
    AssessmentPublishedEventStatusEnum status,
    DateTime? periodStart,
    DateTime? periodEnd,
    String? headline,
    bool? private,
    DateTime at,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAssessmentPublishedEvent.copyWith(...)` or call `instanceOfAssessmentPublishedEvent.copyWith.fieldName(value)` for a single field.
class _$AssessmentPublishedEventCWProxyImpl
    implements _$AssessmentPublishedEventCWProxy {
  const _$AssessmentPublishedEventCWProxyImpl(this._value);

  final AssessmentPublishedEvent _value;

  @override
  AssessmentPublishedEvent assessmentId(String assessmentId) =>
      call(assessmentId: assessmentId);

  @override
  AssessmentPublishedEvent cycleId(String? cycleId) => call(cycleId: cycleId);

  @override
  AssessmentPublishedEvent situationId(String? situationId) =>
      call(situationId: situationId);

  @override
  AssessmentPublishedEvent cadence(
    AssessmentPublishedEventCadenceEnum cadence,
  ) => call(cadence: cadence);

  @override
  AssessmentPublishedEvent status(AssessmentPublishedEventStatusEnum status) =>
      call(status: status);

  @override
  AssessmentPublishedEvent periodStart(DateTime? periodStart) =>
      call(periodStart: periodStart);

  @override
  AssessmentPublishedEvent periodEnd(DateTime? periodEnd) =>
      call(periodEnd: periodEnd);

  @override
  AssessmentPublishedEvent headline(String? headline) =>
      call(headline: headline);

  @override
  AssessmentPublishedEvent private(bool? private) => call(private: private);

  @override
  AssessmentPublishedEvent at(DateTime at) => call(at: at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AssessmentPublishedEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AssessmentPublishedEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AssessmentPublishedEvent call({
    Object? assessmentId = const $CopyWithPlaceholder(),
    Object? cycleId = const $CopyWithPlaceholder(),
    Object? situationId = const $CopyWithPlaceholder(),
    Object? cadence = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? periodStart = const $CopyWithPlaceholder(),
    Object? periodEnd = const $CopyWithPlaceholder(),
    Object? headline = const $CopyWithPlaceholder(),
    Object? private = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
  }) {
    return AssessmentPublishedEvent(
      assessmentId:
          assessmentId == const $CopyWithPlaceholder() || assessmentId == null
          ? _value.assessmentId
          // ignore: cast_nullable_to_non_nullable
          : assessmentId as String,
      cycleId: cycleId == const $CopyWithPlaceholder()
          ? _value.cycleId
          // ignore: cast_nullable_to_non_nullable
          : cycleId as String?,
      situationId: situationId == const $CopyWithPlaceholder()
          ? _value.situationId
          // ignore: cast_nullable_to_non_nullable
          : situationId as String?,
      cadence: cadence == const $CopyWithPlaceholder() || cadence == null
          ? _value.cadence
          // ignore: cast_nullable_to_non_nullable
          : cadence as AssessmentPublishedEventCadenceEnum,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as AssessmentPublishedEventStatusEnum,
      periodStart: periodStart == const $CopyWithPlaceholder()
          ? _value.periodStart
          // ignore: cast_nullable_to_non_nullable
          : periodStart as DateTime?,
      periodEnd: periodEnd == const $CopyWithPlaceholder()
          ? _value.periodEnd
          // ignore: cast_nullable_to_non_nullable
          : periodEnd as DateTime?,
      headline: headline == const $CopyWithPlaceholder()
          ? _value.headline
          // ignore: cast_nullable_to_non_nullable
          : headline as String?,
      private: private == const $CopyWithPlaceholder()
          ? _value.private
          // ignore: cast_nullable_to_non_nullable
          : private as bool?,
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
    );
  }
}

extension $AssessmentPublishedEventCopyWith on AssessmentPublishedEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAssessmentPublishedEvent.copyWith(...)` or `instanceOfAssessmentPublishedEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AssessmentPublishedEventCWProxy get copyWith =>
      _$AssessmentPublishedEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AssessmentPublishedEvent _$AssessmentPublishedEventFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AssessmentPublishedEvent', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['assessmentId', 'cadence', 'status', 'at'],
  );
  final val = AssessmentPublishedEvent(
    assessmentId: $checkedConvert('assessmentId', (v) => v as String),
    cycleId: $checkedConvert('cycleId', (v) => v as String?),
    situationId: $checkedConvert('situationId', (v) => v as String?),
    cadence: $checkedConvert(
      'cadence',
      (v) => $enumDecode(_$AssessmentPublishedEventCadenceEnumEnumMap, v),
    ),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$AssessmentPublishedEventStatusEnumEnumMap, v),
    ),
    periodStart: $checkedConvert(
      'periodStart',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    periodEnd: $checkedConvert(
      'periodEnd',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    headline: $checkedConvert('headline', (v) => v as String?),
    private: $checkedConvert('private', (v) => v as bool?),
    at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
  );
  return val;
});

Map<String, dynamic> _$AssessmentPublishedEventToJson(
  AssessmentPublishedEvent instance,
) => <String, dynamic>{
  'assessmentId': instance.assessmentId,
  'cycleId': ?instance.cycleId,
  'situationId': ?instance.situationId,
  'cadence': _$AssessmentPublishedEventCadenceEnumEnumMap[instance.cadence]!,
  'status': _$AssessmentPublishedEventStatusEnumEnumMap[instance.status]!,
  'periodStart': ?instance.periodStart?.toIso8601String(),
  'periodEnd': ?instance.periodEnd?.toIso8601String(),
  'headline': ?instance.headline,
  'private': ?instance.private,
  'at': instance.at.toIso8601String(),
};

const _$AssessmentPublishedEventCadenceEnumEnumMap = {
  AssessmentPublishedEventCadenceEnum.daily: 'daily',
  AssessmentPublishedEventCadenceEnum.weekly: 'weekly',
  AssessmentPublishedEventCadenceEnum.monthly: 'monthly',
  AssessmentPublishedEventCadenceEnum.onDemand: 'on-demand',
};

const _$AssessmentPublishedEventStatusEnumEnumMap = {
  AssessmentPublishedEventStatusEnum.ok: 'ok',
  AssessmentPublishedEventStatusEnum.partial: 'partial',
  AssessmentPublishedEventStatusEnum.digestOnly: 'digest-only',
  AssessmentPublishedEventStatusEnum.failed: 'failed',
};
