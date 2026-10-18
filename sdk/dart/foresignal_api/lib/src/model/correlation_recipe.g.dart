// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'correlation_recipe.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CorrelationRecipeCWProxy {
  CorrelationRecipe id(String id);

  CorrelationRecipe version(int version);

  CorrelationRecipe builtIn(bool? builtIn);

  CorrelationRecipe enabled(bool enabled);

  CorrelationRecipe definition(CorrelationRecipeDefinition definition);

  CorrelationRecipe params(Map<String, Object>? params);

  CorrelationRecipe disabledReason(String? disabledReason);

  CorrelationRecipe lastRunAt(DateTime? lastRunAt);

  CorrelationRecipe lastError(String? lastError);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CorrelationRecipe(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CorrelationRecipe(...).copyWith(id: 12, name: "My name")
  /// ```
  CorrelationRecipe call({
    String id,
    int version,
    bool? builtIn,
    bool enabled,
    CorrelationRecipeDefinition definition,
    Map<String, Object>? params,
    String? disabledReason,
    DateTime? lastRunAt,
    String? lastError,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCorrelationRecipe.copyWith(...)` or call `instanceOfCorrelationRecipe.copyWith.fieldName(value)` for a single field.
class _$CorrelationRecipeCWProxyImpl implements _$CorrelationRecipeCWProxy {
  const _$CorrelationRecipeCWProxyImpl(this._value);

  final CorrelationRecipe _value;

  @override
  CorrelationRecipe id(String id) => call(id: id);

  @override
  CorrelationRecipe version(int version) => call(version: version);

  @override
  CorrelationRecipe builtIn(bool? builtIn) => call(builtIn: builtIn);

  @override
  CorrelationRecipe enabled(bool enabled) => call(enabled: enabled);

  @override
  CorrelationRecipe definition(CorrelationRecipeDefinition definition) =>
      call(definition: definition);

  @override
  CorrelationRecipe params(Map<String, Object>? params) => call(params: params);

  @override
  CorrelationRecipe disabledReason(String? disabledReason) =>
      call(disabledReason: disabledReason);

  @override
  CorrelationRecipe lastRunAt(DateTime? lastRunAt) =>
      call(lastRunAt: lastRunAt);

  @override
  CorrelationRecipe lastError(String? lastError) => call(lastError: lastError);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CorrelationRecipe(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CorrelationRecipe(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CorrelationRecipe call({
    Object? id = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
    Object? builtIn = const $CopyWithPlaceholder(),
    Object? enabled = const $CopyWithPlaceholder(),
    Object? definition = const $CopyWithPlaceholder(),
    Object? params = const $CopyWithPlaceholder(),
    Object? disabledReason = const $CopyWithPlaceholder(),
    Object? lastRunAt = const $CopyWithPlaceholder(),
    Object? lastError = const $CopyWithPlaceholder(),
  }) {
    return CorrelationRecipe(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      version: version == const $CopyWithPlaceholder() || version == null
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as int,
      builtIn: builtIn == const $CopyWithPlaceholder()
          ? _value.builtIn
          // ignore: cast_nullable_to_non_nullable
          : builtIn as bool?,
      enabled: enabled == const $CopyWithPlaceholder() || enabled == null
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool,
      definition:
          definition == const $CopyWithPlaceholder() || definition == null
          ? _value.definition
          // ignore: cast_nullable_to_non_nullable
          : definition as CorrelationRecipeDefinition,
      params: params == const $CopyWithPlaceholder()
          ? _value.params
          // ignore: cast_nullable_to_non_nullable
          : params as Map<String, Object>?,
      disabledReason: disabledReason == const $CopyWithPlaceholder()
          ? _value.disabledReason
          // ignore: cast_nullable_to_non_nullable
          : disabledReason as String?,
      lastRunAt: lastRunAt == const $CopyWithPlaceholder()
          ? _value.lastRunAt
          // ignore: cast_nullable_to_non_nullable
          : lastRunAt as DateTime?,
      lastError: lastError == const $CopyWithPlaceholder()
          ? _value.lastError
          // ignore: cast_nullable_to_non_nullable
          : lastError as String?,
    );
  }
}

extension $CorrelationRecipeCopyWith on CorrelationRecipe {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCorrelationRecipe.copyWith(...)` or `instanceOfCorrelationRecipe.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CorrelationRecipeCWProxy get copyWith =>
      _$CorrelationRecipeCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CorrelationRecipe _$CorrelationRecipeFromJson(Map<String, dynamic> json) =>
    $checkedCreate('CorrelationRecipe', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['id', 'version', 'enabled', 'definition'],
      );
      final val = CorrelationRecipe(
        id: $checkedConvert('id', (v) => v as String),
        version: $checkedConvert('version', (v) => (v as num).toInt()),
        builtIn: $checkedConvert('builtIn', (v) => v as bool?),
        enabled: $checkedConvert('enabled', (v) => v as bool),
        definition: $checkedConvert(
          'definition',
          (v) =>
              CorrelationRecipeDefinition.fromJson(v as Map<String, dynamic>),
        ),
        params: $checkedConvert(
          'params',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, e as Object),
          ),
        ),
        disabledReason: $checkedConvert('disabledReason', (v) => v as String?),
        lastRunAt: $checkedConvert(
          'lastRunAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        lastError: $checkedConvert('lastError', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$CorrelationRecipeToJson(CorrelationRecipe instance) =>
    <String, dynamic>{
      'id': instance.id,
      'version': instance.version,
      'builtIn': ?instance.builtIn,
      'enabled': instance.enabled,
      'definition': instance.definition.toJson(),
      'params': ?instance.params,
      'disabledReason': ?instance.disabledReason,
      'lastRunAt': ?instance.lastRunAt?.toIso8601String(),
      'lastError': ?instance.lastError,
    };
