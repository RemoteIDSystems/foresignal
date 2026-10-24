// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'correlation_recipe_update.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CorrelationRecipeUpdateCWProxy {
  CorrelationRecipeUpdate enabled(bool? enabled);

  CorrelationRecipeUpdate params(Map<String, Object>? params);

  CorrelationRecipeUpdate definition(CorrelationRecipeDefinition? definition);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CorrelationRecipeUpdate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CorrelationRecipeUpdate(...).copyWith(id: 12, name: "My name")
  /// ```
  CorrelationRecipeUpdate call({
    bool? enabled,
    Map<String, Object>? params,
    CorrelationRecipeDefinition? definition,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCorrelationRecipeUpdate.copyWith(...)` or call `instanceOfCorrelationRecipeUpdate.copyWith.fieldName(value)` for a single field.
class _$CorrelationRecipeUpdateCWProxyImpl
    implements _$CorrelationRecipeUpdateCWProxy {
  const _$CorrelationRecipeUpdateCWProxyImpl(this._value);

  final CorrelationRecipeUpdate _value;

  @override
  CorrelationRecipeUpdate enabled(bool? enabled) => call(enabled: enabled);

  @override
  CorrelationRecipeUpdate params(Map<String, Object>? params) =>
      call(params: params);

  @override
  CorrelationRecipeUpdate definition(CorrelationRecipeDefinition? definition) =>
      call(definition: definition);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CorrelationRecipeUpdate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CorrelationRecipeUpdate(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CorrelationRecipeUpdate call({
    Object? enabled = const $CopyWithPlaceholder(),
    Object? params = const $CopyWithPlaceholder(),
    Object? definition = const $CopyWithPlaceholder(),
  }) {
    return CorrelationRecipeUpdate(
      enabled: enabled == const $CopyWithPlaceholder()
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool?,
      params: params == const $CopyWithPlaceholder()
          ? _value.params
          // ignore: cast_nullable_to_non_nullable
          : params as Map<String, Object>?,
      definition: definition == const $CopyWithPlaceholder()
          ? _value.definition
          // ignore: cast_nullable_to_non_nullable
          : definition as CorrelationRecipeDefinition?,
    );
  }
}

extension $CorrelationRecipeUpdateCopyWith on CorrelationRecipeUpdate {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCorrelationRecipeUpdate.copyWith(...)` or `instanceOfCorrelationRecipeUpdate.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CorrelationRecipeUpdateCWProxy get copyWith =>
      _$CorrelationRecipeUpdateCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CorrelationRecipeUpdate _$CorrelationRecipeUpdateFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('CorrelationRecipeUpdate', json, ($checkedConvert) {
  final val = CorrelationRecipeUpdate(
    enabled: $checkedConvert('enabled', (v) => v as bool?),
    params: $checkedConvert(
      'params',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    definition: $checkedConvert(
      'definition',
      (v) => v == null
          ? null
          : CorrelationRecipeDefinition.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$CorrelationRecipeUpdateToJson(
  CorrelationRecipeUpdate instance,
) => <String, dynamic>{
  'enabled': ?instance.enabled,
  'params': ?instance.params,
  'definition': ?instance.definition?.toJson(),
};
