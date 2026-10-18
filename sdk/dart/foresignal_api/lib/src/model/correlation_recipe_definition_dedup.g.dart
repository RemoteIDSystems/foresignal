// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'correlation_recipe_definition_dedup.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CorrelationRecipeDefinitionDedupCWProxy {
  CorrelationRecipeDefinitionDedup radiusKm(num? radiusKm);

  CorrelationRecipeDefinitionDedup window(String? window);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CorrelationRecipeDefinitionDedup(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CorrelationRecipeDefinitionDedup(...).copyWith(id: 12, name: "My name")
  /// ```
  CorrelationRecipeDefinitionDedup call({num? radiusKm, String? window});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCorrelationRecipeDefinitionDedup.copyWith(...)` or call `instanceOfCorrelationRecipeDefinitionDedup.copyWith.fieldName(value)` for a single field.
class _$CorrelationRecipeDefinitionDedupCWProxyImpl
    implements _$CorrelationRecipeDefinitionDedupCWProxy {
  const _$CorrelationRecipeDefinitionDedupCWProxyImpl(this._value);

  final CorrelationRecipeDefinitionDedup _value;

  @override
  CorrelationRecipeDefinitionDedup radiusKm(num? radiusKm) =>
      call(radiusKm: radiusKm);

  @override
  CorrelationRecipeDefinitionDedup window(String? window) =>
      call(window: window);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CorrelationRecipeDefinitionDedup(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CorrelationRecipeDefinitionDedup(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CorrelationRecipeDefinitionDedup call({
    Object? radiusKm = const $CopyWithPlaceholder(),
    Object? window = const $CopyWithPlaceholder(),
  }) {
    return CorrelationRecipeDefinitionDedup(
      radiusKm: radiusKm == const $CopyWithPlaceholder()
          ? _value.radiusKm
          // ignore: cast_nullable_to_non_nullable
          : radiusKm as num?,
      window: window == const $CopyWithPlaceholder()
          ? _value.window
          // ignore: cast_nullable_to_non_nullable
          : window as String?,
    );
  }
}

extension $CorrelationRecipeDefinitionDedupCopyWith
    on CorrelationRecipeDefinitionDedup {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCorrelationRecipeDefinitionDedup.copyWith(...)` or `instanceOfCorrelationRecipeDefinitionDedup.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CorrelationRecipeDefinitionDedupCWProxy get copyWith =>
      _$CorrelationRecipeDefinitionDedupCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CorrelationRecipeDefinitionDedup _$CorrelationRecipeDefinitionDedupFromJson(
  Map<String, dynamic> json,
) =>
    $checkedCreate('CorrelationRecipeDefinitionDedup', json, ($checkedConvert) {
      final val = CorrelationRecipeDefinitionDedup(
        radiusKm: $checkedConvert('radiusKm', (v) => v as num?),
        window: $checkedConvert('window', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$CorrelationRecipeDefinitionDedupToJson(
  CorrelationRecipeDefinitionDedup instance,
) => <String, dynamic>{
  'radiusKm': ?instance.radiusKm,
  'window': ?instance.window,
};
