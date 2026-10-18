// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'regional_index_def_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RegionalIndexDefInputCWProxy {
  RegionalIndexDefInput name(String name);

  RegionalIndexDefInput description(String? description);

  RegionalIndexDefInput components(
    List<RegionalIndexDefComponentsInner> components,
  );

  RegionalIndexDefInput weights(Map<String, num> weights);

  RegionalIndexDefInput floors(List<RegionalIndexDefFloorsInner>? floors);

  RegionalIndexDefInput target(String target);

  RegionalIndexDefInput enabled(bool enabled);

  RegionalIndexDefInput autoApplyFittedWeights(bool? autoApplyFittedWeights);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RegionalIndexDefInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RegionalIndexDefInput(...).copyWith(id: 12, name: "My name")
  /// ```
  RegionalIndexDefInput call({
    String name,
    String? description,
    List<RegionalIndexDefComponentsInner> components,
    Map<String, num> weights,
    List<RegionalIndexDefFloorsInner>? floors,
    String target,
    bool enabled,
    bool? autoApplyFittedWeights,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRegionalIndexDefInput.copyWith(...)` or call `instanceOfRegionalIndexDefInput.copyWith.fieldName(value)` for a single field.
class _$RegionalIndexDefInputCWProxyImpl
    implements _$RegionalIndexDefInputCWProxy {
  const _$RegionalIndexDefInputCWProxyImpl(this._value);

  final RegionalIndexDefInput _value;

  @override
  RegionalIndexDefInput name(String name) => call(name: name);

  @override
  RegionalIndexDefInput description(String? description) =>
      call(description: description);

  @override
  RegionalIndexDefInput components(
    List<RegionalIndexDefComponentsInner> components,
  ) => call(components: components);

  @override
  RegionalIndexDefInput weights(Map<String, num> weights) =>
      call(weights: weights);

  @override
  RegionalIndexDefInput floors(List<RegionalIndexDefFloorsInner>? floors) =>
      call(floors: floors);

  @override
  RegionalIndexDefInput target(String target) => call(target: target);

  @override
  RegionalIndexDefInput enabled(bool enabled) => call(enabled: enabled);

  @override
  RegionalIndexDefInput autoApplyFittedWeights(bool? autoApplyFittedWeights) =>
      call(autoApplyFittedWeights: autoApplyFittedWeights);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RegionalIndexDefInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RegionalIndexDefInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RegionalIndexDefInput call({
    Object? name = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? components = const $CopyWithPlaceholder(),
    Object? weights = const $CopyWithPlaceholder(),
    Object? floors = const $CopyWithPlaceholder(),
    Object? target = const $CopyWithPlaceholder(),
    Object? enabled = const $CopyWithPlaceholder(),
    Object? autoApplyFittedWeights = const $CopyWithPlaceholder(),
  }) {
    return RegionalIndexDefInput(
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
      enabled: enabled == const $CopyWithPlaceholder() || enabled == null
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool,
      autoApplyFittedWeights:
          autoApplyFittedWeights == const $CopyWithPlaceholder()
          ? _value.autoApplyFittedWeights
          // ignore: cast_nullable_to_non_nullable
          : autoApplyFittedWeights as bool?,
    );
  }
}

extension $RegionalIndexDefInputCopyWith on RegionalIndexDefInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRegionalIndexDefInput.copyWith(...)` or `instanceOfRegionalIndexDefInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RegionalIndexDefInputCWProxy get copyWith =>
      _$RegionalIndexDefInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RegionalIndexDefInput _$RegionalIndexDefInputFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RegionalIndexDefInput', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['name', 'components', 'weights', 'target', 'enabled'],
  );
  final val = RegionalIndexDefInput(
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
    enabled: $checkedConvert('enabled', (v) => v as bool),
    autoApplyFittedWeights: $checkedConvert(
      'autoApplyFittedWeights',
      (v) => v as bool? ?? false,
    ),
  );
  return val;
});

Map<String, dynamic> _$RegionalIndexDefInputToJson(
  RegionalIndexDefInput instance,
) => <String, dynamic>{
  'name': instance.name,
  'description': ?instance.description,
  'components': instance.components.map((e) => e.toJson()).toList(),
  'weights': instance.weights,
  'floors': ?instance.floors?.map((e) => e.toJson()).toList(),
  'target': instance.target,
  'enabled': instance.enabled,
  'autoApplyFittedWeights': ?instance.autoApplyFittedWeights,
};
