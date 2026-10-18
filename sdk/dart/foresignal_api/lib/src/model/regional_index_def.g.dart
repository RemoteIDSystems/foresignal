// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'regional_index_def.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RegionalIndexDefCWProxy {
  RegionalIndexDef id(String id);

  RegionalIndexDef name(String name);

  RegionalIndexDef description(String? description);

  RegionalIndexDef components(List<RegionalIndexDefComponentsInner> components);

  RegionalIndexDef weights(Map<String, num> weights);

  RegionalIndexDef floors(List<RegionalIndexDefFloorsInner>? floors);

  RegionalIndexDef target(String target);

  RegionalIndexDef weightsVersion(int weightsVersion);

  RegionalIndexDef weightsOrigin(
    RegionalIndexDefWeightsOriginEnum weightsOrigin,
  );

  RegionalIndexDef autoApplyFittedWeights(bool? autoApplyFittedWeights);

  RegionalIndexDef enabled(bool enabled);

  RegionalIndexDef builtIn(bool? builtIn);

  RegionalIndexDef ready(bool? ready);

  RegionalIndexDef skill(RegionalIndexDefSkill? skill);

  RegionalIndexDef updatedAt(DateTime? updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RegionalIndexDef(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RegionalIndexDef(...).copyWith(id: 12, name: "My name")
  /// ```
  RegionalIndexDef call({
    String id,
    String name,
    String? description,
    List<RegionalIndexDefComponentsInner> components,
    Map<String, num> weights,
    List<RegionalIndexDefFloorsInner>? floors,
    String target,
    int weightsVersion,
    RegionalIndexDefWeightsOriginEnum weightsOrigin,
    bool? autoApplyFittedWeights,
    bool enabled,
    bool? builtIn,
    bool? ready,
    RegionalIndexDefSkill? skill,
    DateTime? updatedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRegionalIndexDef.copyWith(...)` or call `instanceOfRegionalIndexDef.copyWith.fieldName(value)` for a single field.
class _$RegionalIndexDefCWProxyImpl implements _$RegionalIndexDefCWProxy {
  const _$RegionalIndexDefCWProxyImpl(this._value);

  final RegionalIndexDef _value;

  @override
  RegionalIndexDef id(String id) => call(id: id);

  @override
  RegionalIndexDef name(String name) => call(name: name);

  @override
  RegionalIndexDef description(String? description) =>
      call(description: description);

  @override
  RegionalIndexDef components(
    List<RegionalIndexDefComponentsInner> components,
  ) => call(components: components);

  @override
  RegionalIndexDef weights(Map<String, num> weights) => call(weights: weights);

  @override
  RegionalIndexDef floors(List<RegionalIndexDefFloorsInner>? floors) =>
      call(floors: floors);

  @override
  RegionalIndexDef target(String target) => call(target: target);

  @override
  RegionalIndexDef weightsVersion(int weightsVersion) =>
      call(weightsVersion: weightsVersion);

  @override
  RegionalIndexDef weightsOrigin(
    RegionalIndexDefWeightsOriginEnum weightsOrigin,
  ) => call(weightsOrigin: weightsOrigin);

  @override
  RegionalIndexDef autoApplyFittedWeights(bool? autoApplyFittedWeights) =>
      call(autoApplyFittedWeights: autoApplyFittedWeights);

  @override
  RegionalIndexDef enabled(bool enabled) => call(enabled: enabled);

  @override
  RegionalIndexDef builtIn(bool? builtIn) => call(builtIn: builtIn);

  @override
  RegionalIndexDef ready(bool? ready) => call(ready: ready);

  @override
  RegionalIndexDef skill(RegionalIndexDefSkill? skill) => call(skill: skill);

  @override
  RegionalIndexDef updatedAt(DateTime? updatedAt) => call(updatedAt: updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RegionalIndexDef(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RegionalIndexDef(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RegionalIndexDef call({
    Object? id = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? components = const $CopyWithPlaceholder(),
    Object? weights = const $CopyWithPlaceholder(),
    Object? floors = const $CopyWithPlaceholder(),
    Object? target = const $CopyWithPlaceholder(),
    Object? weightsVersion = const $CopyWithPlaceholder(),
    Object? weightsOrigin = const $CopyWithPlaceholder(),
    Object? autoApplyFittedWeights = const $CopyWithPlaceholder(),
    Object? enabled = const $CopyWithPlaceholder(),
    Object? builtIn = const $CopyWithPlaceholder(),
    Object? ready = const $CopyWithPlaceholder(),
    Object? skill = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
  }) {
    return RegionalIndexDef(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      description: description == const $CopyWithPlaceholder()
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String?,
      components:
          components == const $CopyWithPlaceholder() || components == null
          ? _value.components
          // ignore: cast_nullable_to_non_nullable
          : components as List<RegionalIndexDefComponentsInner>,
      weights: weights == const $CopyWithPlaceholder() || weights == null
          ? _value.weights
          // ignore: cast_nullable_to_non_nullable
          : weights as Map<String, num>,
      floors: floors == const $CopyWithPlaceholder()
          ? _value.floors
          // ignore: cast_nullable_to_non_nullable
          : floors as List<RegionalIndexDefFloorsInner>?,
      target: target == const $CopyWithPlaceholder() || target == null
          ? _value.target
          // ignore: cast_nullable_to_non_nullable
          : target as String,
      weightsVersion:
          weightsVersion == const $CopyWithPlaceholder() ||
              weightsVersion == null
          ? _value.weightsVersion
          // ignore: cast_nullable_to_non_nullable
          : weightsVersion as int,
      weightsOrigin:
          weightsOrigin == const $CopyWithPlaceholder() || weightsOrigin == null
          ? _value.weightsOrigin
          // ignore: cast_nullable_to_non_nullable
          : weightsOrigin as RegionalIndexDefWeightsOriginEnum,
      autoApplyFittedWeights:
          autoApplyFittedWeights == const $CopyWithPlaceholder()
          ? _value.autoApplyFittedWeights
          // ignore: cast_nullable_to_non_nullable
          : autoApplyFittedWeights as bool?,
      enabled: enabled == const $CopyWithPlaceholder() || enabled == null
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool,
      builtIn: builtIn == const $CopyWithPlaceholder()
          ? _value.builtIn
          // ignore: cast_nullable_to_non_nullable
          : builtIn as bool?,
      ready: ready == const $CopyWithPlaceholder()
          ? _value.ready
          // ignore: cast_nullable_to_non_nullable
          : ready as bool?,
      skill: skill == const $CopyWithPlaceholder()
          ? _value.skill
          // ignore: cast_nullable_to_non_nullable
          : skill as RegionalIndexDefSkill?,
      updatedAt: updatedAt == const $CopyWithPlaceholder()
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime?,
    );
  }
}

extension $RegionalIndexDefCopyWith on RegionalIndexDef {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRegionalIndexDef.copyWith(...)` or `instanceOfRegionalIndexDef.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RegionalIndexDefCWProxy get copyWith => _$RegionalIndexDefCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RegionalIndexDef _$RegionalIndexDefFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RegionalIndexDef', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'name',
      'components',
      'weights',
      'target',
      'weightsVersion',
      'weightsOrigin',
      'enabled',
      'skill',
    ],
  );
  final val = RegionalIndexDef(
    id: $checkedConvert('id', (v) => v as String),
    name: $checkedConvert('name', (v) => v as String),
    description: $checkedConvert('description', (v) => v as String?),
    components: $checkedConvert(
      'components',
      (v) => (v as List<dynamic>)
          .map(
            (e) => RegionalIndexDefComponentsInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    weights: $checkedConvert('weights', (v) => Map<String, num>.from(v as Map)),
    floors: $checkedConvert(
      'floors',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) =>
                RegionalIndexDefFloorsInner.fromJson(e as Map<String, dynamic>),
          )
          .toList(),
    ),
    target: $checkedConvert('target', (v) => v as String),
    weightsVersion: $checkedConvert(
      'weightsVersion',
      (v) => (v as num).toInt(),
    ),
    weightsOrigin: $checkedConvert(
      'weightsOrigin',
      (v) => $enumDecode(_$RegionalIndexDefWeightsOriginEnumEnumMap, v),
    ),
    autoApplyFittedWeights: $checkedConvert(
      'autoApplyFittedWeights',
      (v) => v as bool? ?? false,
    ),
    enabled: $checkedConvert('enabled', (v) => v as bool),
    builtIn: $checkedConvert('builtIn', (v) => v as bool?),
    ready: $checkedConvert('ready', (v) => v as bool?),
    skill: $checkedConvert(
      'skill',
      (v) => v == null
          ? null
          : RegionalIndexDefSkill.fromJson(v as Map<String, dynamic>),
    ),
    updatedAt: $checkedConvert(
      'updatedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$RegionalIndexDefToJson(RegionalIndexDef instance) =>
    <String, dynamic>{
      'id': instance.id,
      'name': instance.name,
      'description': ?instance.description,
      'components': instance.components.map((e) => e.toJson()).toList(),
      'weights': instance.weights,
      'floors': ?instance.floors?.map((e) => e.toJson()).toList(),
      'target': instance.target,
      'weightsVersion': instance.weightsVersion,
      'weightsOrigin':
          _$RegionalIndexDefWeightsOriginEnumEnumMap[instance.weightsOrigin]!,
      'autoApplyFittedWeights': ?instance.autoApplyFittedWeights,
      'enabled': instance.enabled,
      'builtIn': ?instance.builtIn,
      'ready': ?instance.ready,
      'skill': instance.skill?.toJson(),
      'updatedAt': ?instance.updatedAt?.toIso8601String(),
    };

const _$RegionalIndexDefWeightsOriginEnumEnumMap = {
  RegionalIndexDefWeightsOriginEnum.default_: 'default',
  RegionalIndexDefWeightsOriginEnum.fitted: 'fitted',
  RegionalIndexDefWeightsOriginEnum.operator_: 'operator',
};
