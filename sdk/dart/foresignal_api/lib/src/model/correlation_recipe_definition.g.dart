// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'correlation_recipe_definition.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CorrelationRecipeDefinitionCWProxy {
  CorrelationRecipeDefinition type(CorrelationRecipeDefinitionTypeEnum type);

  CorrelationRecipeDefinition seed(CorrelationRecipeDefinitionSeed seed);

  CorrelationRecipeDefinition joins(
    List<CorrelationRecipeDefinitionJoinsInner>? joins,
  );

  CorrelationRecipeDefinition minMembers(int? minMembers);

  CorrelationRecipeDefinition minSignalFamilies(int? minSignalFamilies);

  CorrelationRecipeDefinition activateAt(
    CorrelationRecipeDefinitionActivateAt? activateAt,
  );

  CorrelationRecipeDefinition contradictions(
    List<CorrelationRecipeDefinitionContradictionsInner>? contradictions,
  );

  CorrelationRecipeDefinition severityMap(Map<String, Severity>? severityMap);

  CorrelationRecipeDefinition dedup(CorrelationRecipeDefinitionDedup? dedup);

  CorrelationRecipeDefinition quietPeriod(String? quietPeriod);

  CorrelationRecipeDefinition reopenWindow(String? reopenWindow);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CorrelationRecipeDefinition(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CorrelationRecipeDefinition(...).copyWith(id: 12, name: "My name")
  /// ```
  CorrelationRecipeDefinition call({
    CorrelationRecipeDefinitionTypeEnum type,
    CorrelationRecipeDefinitionSeed seed,
    List<CorrelationRecipeDefinitionJoinsInner>? joins,
    int? minMembers,
    int? minSignalFamilies,
    CorrelationRecipeDefinitionActivateAt? activateAt,
    List<CorrelationRecipeDefinitionContradictionsInner>? contradictions,
    Map<String, Severity>? severityMap,
    CorrelationRecipeDefinitionDedup? dedup,
    String? quietPeriod,
    String? reopenWindow,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCorrelationRecipeDefinition.copyWith(...)` or call `instanceOfCorrelationRecipeDefinition.copyWith.fieldName(value)` for a single field.
class _$CorrelationRecipeDefinitionCWProxyImpl
    implements _$CorrelationRecipeDefinitionCWProxy {
  const _$CorrelationRecipeDefinitionCWProxyImpl(this._value);

  final CorrelationRecipeDefinition _value;

  @override
  CorrelationRecipeDefinition type(CorrelationRecipeDefinitionTypeEnum type) =>
      call(type: type);

  @override
  CorrelationRecipeDefinition seed(CorrelationRecipeDefinitionSeed seed) =>
      call(seed: seed);

  @override
  CorrelationRecipeDefinition joins(
    List<CorrelationRecipeDefinitionJoinsInner>? joins,
  ) => call(joins: joins);

  @override
  CorrelationRecipeDefinition minMembers(int? minMembers) =>
      call(minMembers: minMembers);

  @override
  CorrelationRecipeDefinition minSignalFamilies(int? minSignalFamilies) =>
      call(minSignalFamilies: minSignalFamilies);

  @override
  CorrelationRecipeDefinition activateAt(
    CorrelationRecipeDefinitionActivateAt? activateAt,
  ) => call(activateAt: activateAt);

  @override
  CorrelationRecipeDefinition contradictions(
    List<CorrelationRecipeDefinitionContradictionsInner>? contradictions,
  ) => call(contradictions: contradictions);

  @override
  CorrelationRecipeDefinition severityMap(Map<String, Severity>? severityMap) =>
      call(severityMap: severityMap);

  @override
  CorrelationRecipeDefinition dedup(CorrelationRecipeDefinitionDedup? dedup) =>
      call(dedup: dedup);

  @override
  CorrelationRecipeDefinition quietPeriod(String? quietPeriod) =>
      call(quietPeriod: quietPeriod);

  @override
  CorrelationRecipeDefinition reopenWindow(String? reopenWindow) =>
      call(reopenWindow: reopenWindow);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CorrelationRecipeDefinition(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CorrelationRecipeDefinition(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CorrelationRecipeDefinition call({
    Object? type = const $CopyWithPlaceholder(),
    Object? seed = const $CopyWithPlaceholder(),
    Object? joins = const $CopyWithPlaceholder(),
    Object? minMembers = const $CopyWithPlaceholder(),
    Object? minSignalFamilies = const $CopyWithPlaceholder(),
    Object? activateAt = const $CopyWithPlaceholder(),
    Object? contradictions = const $CopyWithPlaceholder(),
    Object? severityMap = const $CopyWithPlaceholder(),
    Object? dedup = const $CopyWithPlaceholder(),
    Object? quietPeriod = const $CopyWithPlaceholder(),
    Object? reopenWindow = const $CopyWithPlaceholder(),
  }) {
    return CorrelationRecipeDefinition(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as CorrelationRecipeDefinitionTypeEnum,
      seed: seed == const $CopyWithPlaceholder() || seed == null
          ? _value.seed
          // ignore: cast_nullable_to_non_nullable
          : seed as CorrelationRecipeDefinitionSeed,
      joins: joins == const $CopyWithPlaceholder()
          ? _value.joins
          // ignore: cast_nullable_to_non_nullable
          : joins as List<CorrelationRecipeDefinitionJoinsInner>?,
      minMembers: minMembers == const $CopyWithPlaceholder()
          ? _value.minMembers
          // ignore: cast_nullable_to_non_nullable
          : minMembers as int?,
      minSignalFamilies: minSignalFamilies == const $CopyWithPlaceholder()
          ? _value.minSignalFamilies
          // ignore: cast_nullable_to_non_nullable
          : minSignalFamilies as int?,
      activateAt: activateAt == const $CopyWithPlaceholder()
          ? _value.activateAt
          // ignore: cast_nullable_to_non_nullable
          : activateAt as CorrelationRecipeDefinitionActivateAt?,
      contradictions: contradictions == const $CopyWithPlaceholder()
          ? _value.contradictions
          // ignore: cast_nullable_to_non_nullable
          : contradictions
                as List<CorrelationRecipeDefinitionContradictionsInner>?,
      severityMap: severityMap == const $CopyWithPlaceholder()
          ? _value.severityMap
          // ignore: cast_nullable_to_non_nullable
          : severityMap as Map<String, Severity>?,
      dedup: dedup == const $CopyWithPlaceholder()
          ? _value.dedup
          // ignore: cast_nullable_to_non_nullable
          : dedup as CorrelationRecipeDefinitionDedup?,
      quietPeriod: quietPeriod == const $CopyWithPlaceholder()
          ? _value.quietPeriod
          // ignore: cast_nullable_to_non_nullable
          : quietPeriod as String?,
      reopenWindow: reopenWindow == const $CopyWithPlaceholder()
          ? _value.reopenWindow
          // ignore: cast_nullable_to_non_nullable
          : reopenWindow as String?,
    );
  }
}

extension $CorrelationRecipeDefinitionCopyWith on CorrelationRecipeDefinition {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCorrelationRecipeDefinition.copyWith(...)` or `instanceOfCorrelationRecipeDefinition.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CorrelationRecipeDefinitionCWProxy get copyWith =>
      _$CorrelationRecipeDefinitionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CorrelationRecipeDefinition _$CorrelationRecipeDefinitionFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('CorrelationRecipeDefinition', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['type', 'seed']);
  final val = CorrelationRecipeDefinition(
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$CorrelationRecipeDefinitionTypeEnumEnumMap, v),
    ),
    seed: $checkedConvert(
      'seed',
      (v) =>
          CorrelationRecipeDefinitionSeed.fromJson(v as Map<String, dynamic>),
    ),
    joins: $checkedConvert(
      'joins',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => CorrelationRecipeDefinitionJoinsInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    minMembers: $checkedConvert('minMembers', (v) => (v as num?)?.toInt()),
    minSignalFamilies: $checkedConvert(
      'minSignalFamilies',
      (v) => (v as num?)?.toInt(),
    ),
    activateAt: $checkedConvert(
      'activateAt',
      (v) => v == null
          ? null
          : CorrelationRecipeDefinitionActivateAt.fromJson(
              v as Map<String, dynamic>,
            ),
    ),
    contradictions: $checkedConvert(
      'contradictions',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => CorrelationRecipeDefinitionContradictionsInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    severityMap: $checkedConvert(
      'severityMap',
      (v) => (v as Map<String, dynamic>?)?.map(
        (k, e) => MapEntry(k, $enumDecode(_$SeverityEnumMap, e)),
      ),
    ),
    dedup: $checkedConvert(
      'dedup',
      (v) => v == null
          ? null
          : CorrelationRecipeDefinitionDedup.fromJson(
              v as Map<String, dynamic>,
            ),
    ),
    quietPeriod: $checkedConvert('quietPeriod', (v) => v as String?),
    reopenWindow: $checkedConvert('reopenWindow', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$CorrelationRecipeDefinitionToJson(
  CorrelationRecipeDefinition instance,
) => <String, dynamic>{
  'type': _$CorrelationRecipeDefinitionTypeEnumEnumMap[instance.type]!,
  'seed': instance.seed.toJson(),
  'joins': ?instance.joins?.map((e) => e.toJson()).toList(),
  'minMembers': ?instance.minMembers,
  'minSignalFamilies': ?instance.minSignalFamilies,
  'activateAt': ?instance.activateAt?.toJson(),
  'contradictions': ?instance.contradictions?.map((e) => e.toJson()).toList(),
  'severityMap': ?instance.severityMap?.map(
    (k, e) => MapEntry(k, _$SeverityEnumMap[e]!),
  ),
  'dedup': ?instance.dedup?.toJson(),
  'quietPeriod': ?instance.quietPeriod,
  'reopenWindow': ?instance.reopenWindow,
};

const _$CorrelationRecipeDefinitionTypeEnumEnumMap = {
  CorrelationRecipeDefinitionTypeEnum.maritimeDisruption: 'maritime-disruption',
  CorrelationRecipeDefinitionTypeEnum.aviationAnomaly: 'aviation-anomaly',
  CorrelationRecipeDefinitionTypeEnum.infrastructureDisruption:
      'infrastructure-disruption',
  CorrelationRecipeDefinitionTypeEnum.weatherImpact: 'weather-impact',
  CorrelationRecipeDefinitionTypeEnum.seismic: 'seismic',
  CorrelationRecipeDefinitionTypeEnum.custom: 'custom',
};

const _$SeverityEnumMap = {
  Severity.info: 'info',
  Severity.warning: 'warning',
  Severity.critical: 'critical',
};
