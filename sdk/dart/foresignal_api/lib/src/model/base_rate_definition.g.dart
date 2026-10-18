// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'base_rate_definition.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$BaseRateDefinitionCWProxy {
  BaseRateDefinition family(String family);

  BaseRateDefinition regionCodes(List<String>? regionCodes);

  BaseRateDefinition regionClass(String? regionClass);

  BaseRateDefinition window(BaseRateDefinitionWindowEnum window);

  BaseRateDefinition condition(BaseRateDefinitionCondition? condition);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BaseRateDefinition(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BaseRateDefinition(...).copyWith(id: 12, name: "My name")
  /// ```
  BaseRateDefinition call({
    String family,
    List<String>? regionCodes,
    String? regionClass,
    BaseRateDefinitionWindowEnum window,
    BaseRateDefinitionCondition? condition,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfBaseRateDefinition.copyWith(...)` or call `instanceOfBaseRateDefinition.copyWith.fieldName(value)` for a single field.
class _$BaseRateDefinitionCWProxyImpl implements _$BaseRateDefinitionCWProxy {
  const _$BaseRateDefinitionCWProxyImpl(this._value);

  final BaseRateDefinition _value;

  @override
  BaseRateDefinition family(String family) => call(family: family);

  @override
  BaseRateDefinition regionCodes(List<String>? regionCodes) =>
      call(regionCodes: regionCodes);

  @override
  BaseRateDefinition regionClass(String? regionClass) =>
      call(regionClass: regionClass);

  @override
  BaseRateDefinition window(BaseRateDefinitionWindowEnum window) =>
      call(window: window);

  @override
  BaseRateDefinition condition(BaseRateDefinitionCondition? condition) =>
      call(condition: condition);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BaseRateDefinition(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BaseRateDefinition(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  BaseRateDefinition call({
    Object? family = const $CopyWithPlaceholder(),
    Object? regionCodes = const $CopyWithPlaceholder(),
    Object? regionClass = const $CopyWithPlaceholder(),
    Object? window = const $CopyWithPlaceholder(),
    Object? condition = const $CopyWithPlaceholder(),
  }) {
    return BaseRateDefinition(
      family: family == const $CopyWithPlaceholder() || family == null
          ? _value.family
          // ignore: cast_nullable_to_non_nullable
          : family as String,
      regionCodes: regionCodes == const $CopyWithPlaceholder()
          ? _value.regionCodes
          // ignore: cast_nullable_to_non_nullable
          : regionCodes as List<String>?,
      regionClass: regionClass == const $CopyWithPlaceholder()
          ? _value.regionClass
          // ignore: cast_nullable_to_non_nullable
          : regionClass as String?,
      window: window == const $CopyWithPlaceholder() || window == null
          ? _value.window
          // ignore: cast_nullable_to_non_nullable
          : window as BaseRateDefinitionWindowEnum,
      condition: condition == const $CopyWithPlaceholder()
          ? _value.condition
          // ignore: cast_nullable_to_non_nullable
          : condition as BaseRateDefinitionCondition?,
    );
  }
}

extension $BaseRateDefinitionCopyWith on BaseRateDefinition {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfBaseRateDefinition.copyWith(...)` or `instanceOfBaseRateDefinition.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$BaseRateDefinitionCWProxy get copyWith =>
      _$BaseRateDefinitionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BaseRateDefinition _$BaseRateDefinitionFromJson(Map<String, dynamic> json) =>
    $checkedCreate('BaseRateDefinition', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['family', 'window']);
      final val = BaseRateDefinition(
        family: $checkedConvert('family', (v) => v as String),
        regionCodes: $checkedConvert(
          'regionCodes',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        regionClass: $checkedConvert('regionClass', (v) => v as String?),
        window: $checkedConvert(
          'window',
          (v) => $enumDecode(_$BaseRateDefinitionWindowEnumEnumMap, v),
        ),
        condition: $checkedConvert(
          'condition',
          (v) => v == null
              ? null
              : BaseRateDefinitionCondition.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$BaseRateDefinitionToJson(BaseRateDefinition instance) =>
    <String, dynamic>{
      'family': instance.family,
      'regionCodes': ?instance.regionCodes,
      'regionClass': ?instance.regionClass,
      'window': _$BaseRateDefinitionWindowEnumEnumMap[instance.window]!,
      'condition': ?instance.condition?.toJson(),
    };

const _$BaseRateDefinitionWindowEnumEnumMap = {
  BaseRateDefinitionWindowEnum.P7D: 'P7D',
  BaseRateDefinitionWindowEnum.P28D: 'P28D',
  BaseRateDefinitionWindowEnum.P90D: 'P90D',
};
