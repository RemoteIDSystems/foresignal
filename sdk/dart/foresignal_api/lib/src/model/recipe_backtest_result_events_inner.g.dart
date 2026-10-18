// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'recipe_backtest_result_events_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RecipeBacktestResultEventsInnerCWProxy {
  RecipeBacktestResultEventsInner type(
    RecipeBacktestResultEventsInnerTypeEnum type,
  );

  RecipeBacktestResultEventsInner startTime(DateTime startTime);

  RecipeBacktestResultEventsInner endTime(DateTime? endTime);

  RecipeBacktestResultEventsInner members(List<IntelEventMember> members);

  RecipeBacktestResultEventsInner confidence(num? confidence);

  RecipeBacktestResultEventsInner evidenceQuality(
    RecipeBacktestResultEventsInnerEvidenceQualityEnum? evidenceQuality,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RecipeBacktestResultEventsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RecipeBacktestResultEventsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  RecipeBacktestResultEventsInner call({
    RecipeBacktestResultEventsInnerTypeEnum type,
    DateTime startTime,
    DateTime? endTime,
    List<IntelEventMember> members,
    num? confidence,
    RecipeBacktestResultEventsInnerEvidenceQualityEnum? evidenceQuality,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRecipeBacktestResultEventsInner.copyWith(...)` or call `instanceOfRecipeBacktestResultEventsInner.copyWith.fieldName(value)` for a single field.
class _$RecipeBacktestResultEventsInnerCWProxyImpl
    implements _$RecipeBacktestResultEventsInnerCWProxy {
  const _$RecipeBacktestResultEventsInnerCWProxyImpl(this._value);

  final RecipeBacktestResultEventsInner _value;

  @override
  RecipeBacktestResultEventsInner type(
    RecipeBacktestResultEventsInnerTypeEnum type,
  ) => call(type: type);

  @override
  RecipeBacktestResultEventsInner startTime(DateTime startTime) =>
      call(startTime: startTime);

  @override
  RecipeBacktestResultEventsInner endTime(DateTime? endTime) =>
      call(endTime: endTime);

  @override
  RecipeBacktestResultEventsInner members(List<IntelEventMember> members) =>
      call(members: members);

  @override
  RecipeBacktestResultEventsInner confidence(num? confidence) =>
      call(confidence: confidence);

  @override
  RecipeBacktestResultEventsInner evidenceQuality(
    RecipeBacktestResultEventsInnerEvidenceQualityEnum? evidenceQuality,
  ) => call(evidenceQuality: evidenceQuality);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RecipeBacktestResultEventsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RecipeBacktestResultEventsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RecipeBacktestResultEventsInner call({
    Object? type = const $CopyWithPlaceholder(),
    Object? startTime = const $CopyWithPlaceholder(),
    Object? endTime = const $CopyWithPlaceholder(),
    Object? members = const $CopyWithPlaceholder(),
    Object? confidence = const $CopyWithPlaceholder(),
    Object? evidenceQuality = const $CopyWithPlaceholder(),
  }) {
    return RecipeBacktestResultEventsInner(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as RecipeBacktestResultEventsInnerTypeEnum,
      startTime: startTime == const $CopyWithPlaceholder() || startTime == null
          ? _value.startTime
          // ignore: cast_nullable_to_non_nullable
          : startTime as DateTime,
      endTime: endTime == const $CopyWithPlaceholder()
          ? _value.endTime
          // ignore: cast_nullable_to_non_nullable
          : endTime as DateTime?,
      members: members == const $CopyWithPlaceholder() || members == null
          ? _value.members
          // ignore: cast_nullable_to_non_nullable
          : members as List<IntelEventMember>,
      confidence: confidence == const $CopyWithPlaceholder()
          ? _value.confidence
          // ignore: cast_nullable_to_non_nullable
          : confidence as num?,
      evidenceQuality: evidenceQuality == const $CopyWithPlaceholder()
          ? _value.evidenceQuality
          // ignore: cast_nullable_to_non_nullable
          : evidenceQuality
                as RecipeBacktestResultEventsInnerEvidenceQualityEnum?,
    );
  }
}

extension $RecipeBacktestResultEventsInnerCopyWith
    on RecipeBacktestResultEventsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRecipeBacktestResultEventsInner.copyWith(...)` or `instanceOfRecipeBacktestResultEventsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RecipeBacktestResultEventsInnerCWProxy get copyWith =>
      _$RecipeBacktestResultEventsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RecipeBacktestResultEventsInner _$RecipeBacktestResultEventsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RecipeBacktestResultEventsInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['type', 'startTime', 'members']);
  final val = RecipeBacktestResultEventsInner(
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$RecipeBacktestResultEventsInnerTypeEnumEnumMap, v),
    ),
    startTime: $checkedConvert('startTime', (v) => DateTime.parse(v as String)),
    endTime: $checkedConvert(
      'endTime',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    members: $checkedConvert(
      'members',
      (v) => (v as List<dynamic>)
          .map((e) => IntelEventMember.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    confidence: $checkedConvert('confidence', (v) => v as num?),
    evidenceQuality: $checkedConvert(
      'evidenceQuality',
      (v) => $enumDecodeNullable(
        _$RecipeBacktestResultEventsInnerEvidenceQualityEnumEnumMap,
        v,
      ),
    ),
  );
  return val;
});

Map<String, dynamic> _$RecipeBacktestResultEventsInnerToJson(
  RecipeBacktestResultEventsInner instance,
) => <String, dynamic>{
  'type': _$RecipeBacktestResultEventsInnerTypeEnumEnumMap[instance.type]!,
  'startTime': instance.startTime.toIso8601String(),
  'endTime': ?instance.endTime?.toIso8601String(),
  'members': instance.members.map((e) => e.toJson()).toList(),
  'confidence': ?instance.confidence,
  'evidenceQuality':
      ?_$RecipeBacktestResultEventsInnerEvidenceQualityEnumEnumMap[instance
          .evidenceQuality],
};

const _$RecipeBacktestResultEventsInnerTypeEnumEnumMap = {
  RecipeBacktestResultEventsInnerTypeEnum.maritimeDisruption:
      'maritime-disruption',
  RecipeBacktestResultEventsInnerTypeEnum.aviationAnomaly: 'aviation-anomaly',
  RecipeBacktestResultEventsInnerTypeEnum.infrastructureDisruption:
      'infrastructure-disruption',
  RecipeBacktestResultEventsInnerTypeEnum.weatherImpact: 'weather-impact',
  RecipeBacktestResultEventsInnerTypeEnum.seismic: 'seismic',
  RecipeBacktestResultEventsInnerTypeEnum.custom: 'custom',
};

const _$RecipeBacktestResultEventsInnerEvidenceQualityEnumEnumMap = {
  RecipeBacktestResultEventsInnerEvidenceQualityEnum.strong: 'strong',
  RecipeBacktestResultEventsInnerEvidenceQualityEnum.moderate: 'moderate',
  RecipeBacktestResultEventsInnerEvidenceQualityEnum.weak: 'weak',
  RecipeBacktestResultEventsInnerEvidenceQualityEnum.insufficient:
      'insufficient',
};
