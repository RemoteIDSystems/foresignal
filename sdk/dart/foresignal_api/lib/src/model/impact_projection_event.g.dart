// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'impact_projection_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ImpactProjectionEventCWProxy {
  ImpactProjectionEvent findingId(String findingId);

  ImpactProjectionEvent hazardType(
    ImpactProjectionEventHazardTypeEnum hazardType,
  );

  ImpactProjectionEvent sourceRef(String? sourceRef);

  ImpactProjectionEvent impactFamily(String impactFamily);

  ImpactProjectionEvent admin1(String admin1);

  ImpactProjectionEvent pImpact(num pImpact);

  ImpactProjectionEvent questionId(String? questionId);

  ImpactProjectionEvent supersededBy(String? supersededBy);

  ImpactProjectionEvent at(DateTime at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ImpactProjectionEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ImpactProjectionEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  ImpactProjectionEvent call({
    String findingId,
    ImpactProjectionEventHazardTypeEnum hazardType,
    String? sourceRef,
    String impactFamily,
    String admin1,
    num pImpact,
    String? questionId,
    String? supersededBy,
    DateTime at,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfImpactProjectionEvent.copyWith(...)` or call `instanceOfImpactProjectionEvent.copyWith.fieldName(value)` for a single field.
class _$ImpactProjectionEventCWProxyImpl
    implements _$ImpactProjectionEventCWProxy {
  const _$ImpactProjectionEventCWProxyImpl(this._value);

  final ImpactProjectionEvent _value;

  @override
  ImpactProjectionEvent findingId(String findingId) =>
      call(findingId: findingId);

  @override
  ImpactProjectionEvent hazardType(
    ImpactProjectionEventHazardTypeEnum hazardType,
  ) => call(hazardType: hazardType);

  @override
  ImpactProjectionEvent sourceRef(String? sourceRef) =>
      call(sourceRef: sourceRef);

  @override
  ImpactProjectionEvent impactFamily(String impactFamily) =>
      call(impactFamily: impactFamily);

  @override
  ImpactProjectionEvent admin1(String admin1) => call(admin1: admin1);

  @override
  ImpactProjectionEvent pImpact(num pImpact) => call(pImpact: pImpact);

  @override
  ImpactProjectionEvent questionId(String? questionId) =>
      call(questionId: questionId);

  @override
  ImpactProjectionEvent supersededBy(String? supersededBy) =>
      call(supersededBy: supersededBy);

  @override
  ImpactProjectionEvent at(DateTime at) => call(at: at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ImpactProjectionEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ImpactProjectionEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ImpactProjectionEvent call({
    Object? findingId = const $CopyWithPlaceholder(),
    Object? hazardType = const $CopyWithPlaceholder(),
    Object? sourceRef = const $CopyWithPlaceholder(),
    Object? impactFamily = const $CopyWithPlaceholder(),
    Object? admin1 = const $CopyWithPlaceholder(),
    Object? pImpact = const $CopyWithPlaceholder(),
    Object? questionId = const $CopyWithPlaceholder(),
    Object? supersededBy = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
  }) {
    return ImpactProjectionEvent(
      findingId: findingId == const $CopyWithPlaceholder() || findingId == null
          ? _value.findingId
          // ignore: cast_nullable_to_non_nullable
          : findingId as String,
      hazardType:
          hazardType == const $CopyWithPlaceholder() || hazardType == null
          ? _value.hazardType
          // ignore: cast_nullable_to_non_nullable
          : hazardType as ImpactProjectionEventHazardTypeEnum,
      sourceRef: sourceRef == const $CopyWithPlaceholder()
          ? _value.sourceRef
          // ignore: cast_nullable_to_non_nullable
          : sourceRef as String?,
      impactFamily:
          impactFamily == const $CopyWithPlaceholder() || impactFamily == null
          ? _value.impactFamily
          // ignore: cast_nullable_to_non_nullable
          : impactFamily as String,
      admin1: admin1 == const $CopyWithPlaceholder() || admin1 == null
          ? _value.admin1
          // ignore: cast_nullable_to_non_nullable
          : admin1 as String,
      pImpact: pImpact == const $CopyWithPlaceholder() || pImpact == null
          ? _value.pImpact
          // ignore: cast_nullable_to_non_nullable
          : pImpact as num,
      questionId: questionId == const $CopyWithPlaceholder()
          ? _value.questionId
          // ignore: cast_nullable_to_non_nullable
          : questionId as String?,
      supersededBy: supersededBy == const $CopyWithPlaceholder()
          ? _value.supersededBy
          // ignore: cast_nullable_to_non_nullable
          : supersededBy as String?,
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
    );
  }
}

extension $ImpactProjectionEventCopyWith on ImpactProjectionEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfImpactProjectionEvent.copyWith(...)` or `instanceOfImpactProjectionEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ImpactProjectionEventCWProxy get copyWith =>
      _$ImpactProjectionEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ImpactProjectionEvent _$ImpactProjectionEventFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ImpactProjectionEvent', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'findingId',
      'hazardType',
      'impactFamily',
      'admin1',
      'pImpact',
      'at',
    ],
  );
  final val = ImpactProjectionEvent(
    findingId: $checkedConvert('findingId', (v) => v as String),
    hazardType: $checkedConvert(
      'hazardType',
      (v) => $enumDecode(_$ImpactProjectionEventHazardTypeEnumEnumMap, v),
    ),
    sourceRef: $checkedConvert('sourceRef', (v) => v as String?),
    impactFamily: $checkedConvert('impactFamily', (v) => v as String),
    admin1: $checkedConvert('admin1', (v) => v as String),
    pImpact: $checkedConvert('pImpact', (v) => v as num),
    questionId: $checkedConvert('questionId', (v) => v as String?),
    supersededBy: $checkedConvert('supersededBy', (v) => v as String?),
    at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
  );
  return val;
});

Map<String, dynamic> _$ImpactProjectionEventToJson(
  ImpactProjectionEvent instance,
) => <String, dynamic>{
  'findingId': instance.findingId,
  'hazardType':
      _$ImpactProjectionEventHazardTypeEnumEnumMap[instance.hazardType]!,
  'sourceRef': ?instance.sourceRef,
  'impactFamily': instance.impactFamily,
  'admin1': instance.admin1,
  'pImpact': instance.pImpact,
  'questionId': ?instance.questionId,
  'supersededBy': ?instance.supersededBy,
  'at': instance.at.toIso8601String(),
};

const _$ImpactProjectionEventHazardTypeEnumEnumMap = {
  ImpactProjectionEventHazardTypeEnum.tropicalWind34: 'tropical-wind-34',
  ImpactProjectionEventHazardTypeEnum.tropicalWind50: 'tropical-wind-50',
  ImpactProjectionEventHazardTypeEnum.tropicalWind64: 'tropical-wind-64',
  ImpactProjectionEventHazardTypeEnum.tropicalTrack: 'tropical-track',
  ImpactProjectionEventHazardTypeEnum.tornado: 'tornado',
  ImpactProjectionEventHazardTypeEnum.wind: 'wind',
  ImpactProjectionEventHazardTypeEnum.hail: 'hail',
  ImpactProjectionEventHazardTypeEnum.severeCategorical: 'severe-categorical',
  ImpactProjectionEventHazardTypeEnum.excessiveRain: 'excessive-rain',
  ImpactProjectionEventHazardTypeEnum.riverFlood: 'river-flood',
  ImpactProjectionEventHazardTypeEnum.stormCell: 'storm-cell',
};
