// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'correlation_recipe_definition_contradictions_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CorrelationRecipeDefinitionContradictionsInnerCWProxy {
  CorrelationRecipeDefinitionContradictionsInner kinds(List<String>? kinds);

  CorrelationRecipeDefinitionContradictionsInner healthOf(
    List<String>? healthOf,
  );

  CorrelationRecipeDefinitionContradictionsInner withinKm(num? withinKm);

  CorrelationRecipeDefinitionContradictionsInner window(String? window);

  CorrelationRecipeDefinitionContradictionsInner alternative(
    String alternative,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CorrelationRecipeDefinitionContradictionsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CorrelationRecipeDefinitionContradictionsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  CorrelationRecipeDefinitionContradictionsInner call({
    List<String>? kinds,
    List<String>? healthOf,
    num? withinKm,
    String? window,
    String alternative,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCorrelationRecipeDefinitionContradictionsInner.copyWith(...)` or call `instanceOfCorrelationRecipeDefinitionContradictionsInner.copyWith.fieldName(value)` for a single field.
class _$CorrelationRecipeDefinitionContradictionsInnerCWProxyImpl
    implements _$CorrelationRecipeDefinitionContradictionsInnerCWProxy {
  const _$CorrelationRecipeDefinitionContradictionsInnerCWProxyImpl(
    this._value,
  );

  final CorrelationRecipeDefinitionContradictionsInner _value;

  @override
  CorrelationRecipeDefinitionContradictionsInner kinds(List<String>? kinds) =>
      call(kinds: kinds);

  @override
  CorrelationRecipeDefinitionContradictionsInner healthOf(
    List<String>? healthOf,
  ) => call(healthOf: healthOf);

  @override
  CorrelationRecipeDefinitionContradictionsInner withinKm(num? withinKm) =>
      call(withinKm: withinKm);

  @override
  CorrelationRecipeDefinitionContradictionsInner window(String? window) =>
      call(window: window);

  @override
  CorrelationRecipeDefinitionContradictionsInner alternative(
    String alternative,
  ) => call(alternative: alternative);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CorrelationRecipeDefinitionContradictionsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CorrelationRecipeDefinitionContradictionsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CorrelationRecipeDefinitionContradictionsInner call({
    Object? kinds = const $CopyWithPlaceholder(),
    Object? healthOf = const $CopyWithPlaceholder(),
    Object? withinKm = const $CopyWithPlaceholder(),
    Object? window = const $CopyWithPlaceholder(),
    Object? alternative = const $CopyWithPlaceholder(),
  }) {
    return CorrelationRecipeDefinitionContradictionsInner(
      kinds: kinds == const $CopyWithPlaceholder()
          ? _value.kinds
          // ignore: cast_nullable_to_non_nullable
          : kinds as List<String>?,
      healthOf: healthOf == const $CopyWithPlaceholder()
          ? _value.healthOf
          // ignore: cast_nullable_to_non_nullable
          : healthOf as List<String>?,
      withinKm: withinKm == const $CopyWithPlaceholder()
          ? _value.withinKm
          // ignore: cast_nullable_to_non_nullable
          : withinKm as num?,
      window: window == const $CopyWithPlaceholder()
          ? _value.window
          // ignore: cast_nullable_to_non_nullable
          : window as String?,
      alternative:
          alternative == const $CopyWithPlaceholder() || alternative == null
          ? _value.alternative
          // ignore: cast_nullable_to_non_nullable
          : alternative as String,
    );
  }
}

extension $CorrelationRecipeDefinitionContradictionsInnerCopyWith
    on CorrelationRecipeDefinitionContradictionsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCorrelationRecipeDefinitionContradictionsInner.copyWith(...)` or `instanceOfCorrelationRecipeDefinitionContradictionsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CorrelationRecipeDefinitionContradictionsInnerCWProxy get copyWith =>
      _$CorrelationRecipeDefinitionContradictionsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CorrelationRecipeDefinitionContradictionsInner
_$CorrelationRecipeDefinitionContradictionsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('CorrelationRecipeDefinitionContradictionsInner', json, (
  $checkedConvert,
) {
  $checkKeys(json, requiredKeys: const ['alternative']);
  final val = CorrelationRecipeDefinitionContradictionsInner(
    kinds: $checkedConvert(
      'kinds',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    healthOf: $checkedConvert(
      'healthOf',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    withinKm: $checkedConvert('withinKm', (v) => v as num?),
    window: $checkedConvert('window', (v) => v as String?),
    alternative: $checkedConvert('alternative', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$CorrelationRecipeDefinitionContradictionsInnerToJson(
  CorrelationRecipeDefinitionContradictionsInner instance,
) => <String, dynamic>{
  'kinds': ?instance.kinds,
  'healthOf': ?instance.healthOf,
  'withinKm': ?instance.withinKm,
  'window': ?instance.window,
  'alternative': instance.alternative,
};
