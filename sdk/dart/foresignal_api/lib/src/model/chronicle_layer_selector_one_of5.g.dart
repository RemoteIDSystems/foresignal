// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_layer_selector_one_of5.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleLayerSelectorOneOf5CWProxy {
  ChronicleLayerSelectorOneOf5 type(ChronicleLayerSelectorOneOf5TypeEnum type);

  ChronicleLayerSelectorOneOf5 findingTypes(List<String>? findingTypes);

  ChronicleLayerSelectorOneOf5 families(List<String>? families);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleLayerSelectorOneOf5(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleLayerSelectorOneOf5(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleLayerSelectorOneOf5 call({
    ChronicleLayerSelectorOneOf5TypeEnum type,
    List<String>? findingTypes,
    List<String>? families,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleLayerSelectorOneOf5.copyWith(...)` or call `instanceOfChronicleLayerSelectorOneOf5.copyWith.fieldName(value)` for a single field.
class _$ChronicleLayerSelectorOneOf5CWProxyImpl
    implements _$ChronicleLayerSelectorOneOf5CWProxy {
  const _$ChronicleLayerSelectorOneOf5CWProxyImpl(this._value);

  final ChronicleLayerSelectorOneOf5 _value;

  @override
  ChronicleLayerSelectorOneOf5 type(
    ChronicleLayerSelectorOneOf5TypeEnum type,
  ) => call(type: type);

  @override
  ChronicleLayerSelectorOneOf5 findingTypes(List<String>? findingTypes) =>
      call(findingTypes: findingTypes);

  @override
  ChronicleLayerSelectorOneOf5 families(List<String>? families) =>
      call(families: families);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleLayerSelectorOneOf5(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleLayerSelectorOneOf5(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleLayerSelectorOneOf5 call({
    Object? type = const $CopyWithPlaceholder(),
    Object? findingTypes = const $CopyWithPlaceholder(),
    Object? families = const $CopyWithPlaceholder(),
  }) {
    return ChronicleLayerSelectorOneOf5(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as ChronicleLayerSelectorOneOf5TypeEnum,
      findingTypes: findingTypes == const $CopyWithPlaceholder()
          ? _value.findingTypes
          // ignore: cast_nullable_to_non_nullable
          : findingTypes as List<String>?,
      families: families == const $CopyWithPlaceholder()
          ? _value.families
          // ignore: cast_nullable_to_non_nullable
          : families as List<String>?,
    );
  }
}

extension $ChronicleLayerSelectorOneOf5CopyWith
    on ChronicleLayerSelectorOneOf5 {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleLayerSelectorOneOf5.copyWith(...)` or `instanceOfChronicleLayerSelectorOneOf5.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleLayerSelectorOneOf5CWProxy get copyWith =>
      _$ChronicleLayerSelectorOneOf5CWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleLayerSelectorOneOf5 _$ChronicleLayerSelectorOneOf5FromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChronicleLayerSelectorOneOf5', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['type']);
  final val = ChronicleLayerSelectorOneOf5(
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$ChronicleLayerSelectorOneOf5TypeEnumEnumMap, v),
    ),
    findingTypes: $checkedConvert(
      'findingTypes',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    families: $checkedConvert(
      'families',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$ChronicleLayerSelectorOneOf5ToJson(
  ChronicleLayerSelectorOneOf5 instance,
) => <String, dynamic>{
  'type': _$ChronicleLayerSelectorOneOf5TypeEnumEnumMap[instance.type]!,
  'findingTypes': ?instance.findingTypes,
  'families': ?instance.families,
};

const _$ChronicleLayerSelectorOneOf5TypeEnumEnumMap = {
  ChronicleLayerSelectorOneOf5TypeEnum.findings: 'findings',
};
