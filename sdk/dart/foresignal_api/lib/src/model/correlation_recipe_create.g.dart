// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'correlation_recipe_create.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CorrelationRecipeCreateCWProxy {
  CorrelationRecipeCreate id(String id);

  CorrelationRecipeCreate definition(CorrelationRecipeDefinition definition);

  CorrelationRecipeCreate enabled(bool? enabled);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CorrelationRecipeCreate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CorrelationRecipeCreate(...).copyWith(id: 12, name: "My name")
  /// ```
  CorrelationRecipeCreate call({
    String id,
    CorrelationRecipeDefinition definition,
    bool? enabled,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCorrelationRecipeCreate.copyWith(...)` or call `instanceOfCorrelationRecipeCreate.copyWith.fieldName(value)` for a single field.
class _$CorrelationRecipeCreateCWProxyImpl
    implements _$CorrelationRecipeCreateCWProxy {
  const _$CorrelationRecipeCreateCWProxyImpl(this._value);

  final CorrelationRecipeCreate _value;

  @override
  CorrelationRecipeCreate id(String id) => call(id: id);

  @override
  CorrelationRecipeCreate definition(CorrelationRecipeDefinition definition) =>
      call(definition: definition);

  @override
  CorrelationRecipeCreate enabled(bool? enabled) => call(enabled: enabled);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CorrelationRecipeCreate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CorrelationRecipeCreate(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CorrelationRecipeCreate call({
    Object? id = const $CopyWithPlaceholder(),
    Object? definition = const $CopyWithPlaceholder(),
    Object? enabled = const $CopyWithPlaceholder(),
  }) {
    return CorrelationRecipeCreate(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      definition:
          definition == const $CopyWithPlaceholder() || definition == null
          ? _value.definition
          // ignore: cast_nullable_to_non_nullable
          : definition as CorrelationRecipeDefinition,
      enabled: enabled == const $CopyWithPlaceholder()
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool?,
    );
  }
}

extension $CorrelationRecipeCreateCopyWith on CorrelationRecipeCreate {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCorrelationRecipeCreate.copyWith(...)` or `instanceOfCorrelationRecipeCreate.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CorrelationRecipeCreateCWProxy get copyWith =>
      _$CorrelationRecipeCreateCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CorrelationRecipeCreate _$CorrelationRecipeCreateFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('CorrelationRecipeCreate', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['id', 'definition']);
  final val = CorrelationRecipeCreate(
    id: $checkedConvert('id', (v) => v as String),
    definition: $checkedConvert(
      'definition',
      (v) => CorrelationRecipeDefinition.fromJson(v as Map<String, dynamic>),
    ),
    enabled: $checkedConvert('enabled', (v) => v as bool?),
  );
  return val;
});

Map<String, dynamic> _$CorrelationRecipeCreateToJson(
  CorrelationRecipeCreate instance,
) => <String, dynamic>{
  'id': instance.id,
  'definition': instance.definition.toJson(),
  'enabled': ?instance.enabled,
};
