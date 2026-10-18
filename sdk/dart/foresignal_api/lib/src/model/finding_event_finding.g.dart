// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'finding_event_finding.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$FindingEventFindingCWProxy {
  FindingEventFinding id(String id);

  FindingEventFinding findingType(
    FindingEventFindingFindingTypeEnum findingType,
  );

  FindingEventFinding title(String title);

  FindingEventFinding severity(FindingSeverity severity);

  FindingEventFinding status(FindingEventFindingStatusEnum status);

  FindingEventFinding regionCodes(List<String>? regionCodes);

  FindingEventFinding detectedAt(DateTime detectedAt);

  FindingEventFinding situationId(String? situationId);

  FindingEventFinding private(bool? private);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FindingEventFinding(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FindingEventFinding(...).copyWith(id: 12, name: "My name")
  /// ```
  FindingEventFinding call({
    String id,
    FindingEventFindingFindingTypeEnum findingType,
    String title,
    FindingSeverity severity,
    FindingEventFindingStatusEnum status,
    List<String>? regionCodes,
    DateTime detectedAt,
    String? situationId,
    bool? private,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfFindingEventFinding.copyWith(...)` or call `instanceOfFindingEventFinding.copyWith.fieldName(value)` for a single field.
class _$FindingEventFindingCWProxyImpl implements _$FindingEventFindingCWProxy {
  const _$FindingEventFindingCWProxyImpl(this._value);

  final FindingEventFinding _value;

  @override
  FindingEventFinding id(String id) => call(id: id);

  @override
  FindingEventFinding findingType(
    FindingEventFindingFindingTypeEnum findingType,
  ) => call(findingType: findingType);

  @override
  FindingEventFinding title(String title) => call(title: title);

  @override
  FindingEventFinding severity(FindingSeverity severity) =>
      call(severity: severity);

  @override
  FindingEventFinding status(FindingEventFindingStatusEnum status) =>
      call(status: status);

  @override
  FindingEventFinding regionCodes(List<String>? regionCodes) =>
      call(regionCodes: regionCodes);

  @override
  FindingEventFinding detectedAt(DateTime detectedAt) =>
      call(detectedAt: detectedAt);

  @override
  FindingEventFinding situationId(String? situationId) =>
      call(situationId: situationId);

  @override
  FindingEventFinding private(bool? private) => call(private: private);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FindingEventFinding(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FindingEventFinding(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  FindingEventFinding call({
    Object? id = const $CopyWithPlaceholder(),
    Object? findingType = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? severity = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? regionCodes = const $CopyWithPlaceholder(),
    Object? detectedAt = const $CopyWithPlaceholder(),
    Object? situationId = const $CopyWithPlaceholder(),
    Object? private = const $CopyWithPlaceholder(),
  }) {
    return FindingEventFinding(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      findingType:
          findingType == const $CopyWithPlaceholder() || findingType == null
          ? _value.findingType
          // ignore: cast_nullable_to_non_nullable
          : findingType as FindingEventFindingFindingTypeEnum,
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      severity: severity == const $CopyWithPlaceholder() || severity == null
          ? _value.severity
          // ignore: cast_nullable_to_non_nullable
          : severity as FindingSeverity,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as FindingEventFindingStatusEnum,
      regionCodes: regionCodes == const $CopyWithPlaceholder()
          ? _value.regionCodes
          // ignore: cast_nullable_to_non_nullable
          : regionCodes as List<String>?,
      detectedAt:
          detectedAt == const $CopyWithPlaceholder() || detectedAt == null
          ? _value.detectedAt
          // ignore: cast_nullable_to_non_nullable
          : detectedAt as DateTime,
      situationId: situationId == const $CopyWithPlaceholder()
          ? _value.situationId
          // ignore: cast_nullable_to_non_nullable
          : situationId as String?,
      private: private == const $CopyWithPlaceholder()
          ? _value.private
          // ignore: cast_nullable_to_non_nullable
          : private as bool?,
    );
  }
}

extension $FindingEventFindingCopyWith on FindingEventFinding {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfFindingEventFinding.copyWith(...)` or `instanceOfFindingEventFinding.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$FindingEventFindingCWProxy get copyWith =>
      _$FindingEventFindingCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

FindingEventFinding _$FindingEventFindingFromJson(Map<String, dynamic> json) =>
    $checkedCreate('FindingEventFinding', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'id',
          'findingType',
          'title',
          'severity',
          'status',
          'detectedAt',
        ],
      );
      final val = FindingEventFinding(
        id: $checkedConvert('id', (v) => v as String),
        findingType: $checkedConvert(
          'findingType',
          (v) => $enumDecode(_$FindingEventFindingFindingTypeEnumEnumMap, v),
        ),
        title: $checkedConvert('title', (v) => v as String),
        severity: $checkedConvert(
          'severity',
          (v) => $enumDecode(_$FindingSeverityEnumMap, v),
        ),
        status: $checkedConvert(
          'status',
          (v) => $enumDecode(_$FindingEventFindingStatusEnumEnumMap, v),
        ),
        regionCodes: $checkedConvert(
          'regionCodes',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        detectedAt: $checkedConvert(
          'detectedAt',
          (v) => DateTime.parse(v as String),
        ),
        situationId: $checkedConvert('situationId', (v) => v as String?),
        private: $checkedConvert('private', (v) => v as bool?),
      );
      return val;
    });

Map<String, dynamic> _$FindingEventFindingToJson(
  FindingEventFinding instance,
) => <String, dynamic>{
  'id': instance.id,
  'findingType':
      _$FindingEventFindingFindingTypeEnumEnumMap[instance.findingType]!,
  'title': instance.title,
  'severity': _$FindingSeverityEnumMap[instance.severity]!,
  'status': _$FindingEventFindingStatusEnumEnumMap[instance.status]!,
  'regionCodes': ?instance.regionCodes,
  'detectedAt': instance.detectedAt.toIso8601String(),
  'situationId': ?instance.situationId,
  'private': ?instance.private,
};

const _$FindingEventFindingFindingTypeEnumEnumMap = {
  FindingEventFindingFindingTypeEnum.changePoint: 'change-point',
  FindingEventFindingFindingTypeEnum.preEventSignal: 'pre-event-signal',
  FindingEventFindingFindingTypeEnum.unexplainedMove: 'unexplained-move',
  FindingEventFindingFindingTypeEnum.coOccurrence: 'co-occurrence',
  FindingEventFindingFindingTypeEnum.precursorActive: 'precursor-active',
  FindingEventFindingFindingTypeEnum.forecastExceedance: 'forecast-exceedance',
  FindingEventFindingFindingTypeEnum.outlookExceedance: 'outlook-exceedance',
  FindingEventFindingFindingTypeEnum.projectedImpact: 'projected-impact',
  FindingEventFindingFindingTypeEnum.socialVelocity: 'social-velocity',
  FindingEventFindingFindingTypeEnum.mediaReuse: 'media-reuse',
  FindingEventFindingFindingTypeEnum.socialCoordination: 'social-coordination',
  FindingEventFindingFindingTypeEnum.attentionGap: 'attention-gap',
};

const _$FindingSeverityEnumMap = {
  FindingSeverity.info: 'info',
  FindingSeverity.notable: 'notable',
  FindingSeverity.significant: 'significant',
};

const _$FindingEventFindingStatusEnumEnumMap = {
  FindingEventFindingStatusEnum.open: 'open',
  FindingEventFindingStatusEnum.superseded: 'superseded',
  FindingEventFindingStatusEnum.dismissed: 'dismissed',
};
