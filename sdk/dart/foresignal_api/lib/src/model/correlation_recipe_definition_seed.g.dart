// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'correlation_recipe_definition_seed.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CorrelationRecipeDefinitionSeedCWProxy {
  CorrelationRecipeDefinitionSeed kinds(List<String>? kinds);

  CorrelationRecipeDefinitionSeed sources(List<String>? sources);

  CorrelationRecipeDefinitionSeed conditions(List<Condition>? conditions);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CorrelationRecipeDefinitionSeed(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CorrelationRecipeDefinitionSeed(...).copyWith(id: 12, name: "My name")
  /// ```
  CorrelationRecipeDefinitionSeed call({
    List<String>? kinds,
    List<String>? sources,
    List<Condition>? conditions,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCorrelationRecipeDefinitionSeed.copyWith(...)` or call `instanceOfCorrelationRecipeDefinitionSeed.copyWith.fieldName(value)` for a single field.
class _$CorrelationRecipeDefinitionSeedCWProxyImpl
    implements _$CorrelationRecipeDefinitionSeedCWProxy {
  const _$CorrelationRecipeDefinitionSeedCWProxyImpl(this._value);

  final CorrelationRecipeDefinitionSeed _value;

  @override
  CorrelationRecipeDefinitionSeed kinds(List<String>? kinds) =>
      call(kinds: kinds);

  @override
  CorrelationRecipeDefinitionSeed sources(List<String>? sources) =>
      call(sources: sources);

  @override
  CorrelationRecipeDefinitionSeed conditions(List<Condition>? conditions) =>
      call(conditions: conditions);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CorrelationRecipeDefinitionSeed(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CorrelationRecipeDefinitionSeed(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CorrelationRecipeDefinitionSeed call({
    Object? kinds = const $CopyWithPlaceholder(),
    Object? sources = const $CopyWithPlaceholder(),
    Object? conditions = const $CopyWithPlaceholder(),
  }) {
    return CorrelationRecipeDefinitionSeed(
      kinds: kinds == const $CopyWithPlaceholder()
          ? _value.kinds
          // ignore: cast_nullable_to_non_nullable
          : kinds as List<String>?,
      sources: sources == const $CopyWithPlaceholder()
          ? _value.sources
          // ignore: cast_nullable_to_non_nullable
          : sources as List<String>?,
      conditions: conditions == const $CopyWithPlaceholder()
          ? _value.conditions
          // ignore: cast_nullable_to_non_nullable
          : conditions as List<Condition>?,
    );
  }
}

extension $CorrelationRecipeDefinitionSeedCopyWith
    on CorrelationRecipeDefinitionSeed {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCorrelationRecipeDefinitionSeed.copyWith(...)` or `instanceOfCorrelationRecipeDefinitionSeed.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CorrelationRecipeDefinitionSeedCWProxy get copyWith =>
      _$CorrelationRecipeDefinitionSeedCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CorrelationRecipeDefinitionSeed _$CorrelationRecipeDefinitionSeedFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('CorrelationRecipeDefinitionSeed', json, ($checkedConvert) {
  final val = CorrelationRecipeDefinitionSeed(
    kinds: $checkedConvert(
      'kinds',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    sources: $checkedConvert(
      'sources',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    conditions: $checkedConvert(
      'conditions',
      (v) => (v as List<dynamic>?)
          ?.map((e) => Condition.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$CorrelationRecipeDefinitionSeedToJson(
  CorrelationRecipeDefinitionSeed instance,
) => <String, dynamic>{
  'kinds': ?instance.kinds,
  'sources': ?instance.sources,
  'conditions': ?instance.conditions?.map((e) => e.toJson()).toList(),
};
