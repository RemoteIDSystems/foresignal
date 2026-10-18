// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_layer_selector_one_of8.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleLayerSelectorOneOf8CWProxy {
  ChronicleLayerSelectorOneOf8 type(ChronicleLayerSelectorOneOf8TypeEnum type);

  ChronicleLayerSelectorOneOf8 ruleIds(List<String>? ruleIds);

  ChronicleLayerSelectorOneOf8 tags(List<String>? tags);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleLayerSelectorOneOf8(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleLayerSelectorOneOf8(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleLayerSelectorOneOf8 call({
    ChronicleLayerSelectorOneOf8TypeEnum type,
    List<String>? ruleIds,
    List<String>? tags,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleLayerSelectorOneOf8.copyWith(...)` or call `instanceOfChronicleLayerSelectorOneOf8.copyWith.fieldName(value)` for a single field.
class _$ChronicleLayerSelectorOneOf8CWProxyImpl
    implements _$ChronicleLayerSelectorOneOf8CWProxy {
  const _$ChronicleLayerSelectorOneOf8CWProxyImpl(this._value);

  final ChronicleLayerSelectorOneOf8 _value;

  @override
  ChronicleLayerSelectorOneOf8 type(
    ChronicleLayerSelectorOneOf8TypeEnum type,
  ) => call(type: type);

  @override
  ChronicleLayerSelectorOneOf8 ruleIds(List<String>? ruleIds) =>
      call(ruleIds: ruleIds);

  @override
  ChronicleLayerSelectorOneOf8 tags(List<String>? tags) => call(tags: tags);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleLayerSelectorOneOf8(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleLayerSelectorOneOf8(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleLayerSelectorOneOf8 call({
    Object? type = const $CopyWithPlaceholder(),
    Object? ruleIds = const $CopyWithPlaceholder(),
    Object? tags = const $CopyWithPlaceholder(),
  }) {
    return ChronicleLayerSelectorOneOf8(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as ChronicleLayerSelectorOneOf8TypeEnum,
      ruleIds: ruleIds == const $CopyWithPlaceholder()
          ? _value.ruleIds
          // ignore: cast_nullable_to_non_nullable
          : ruleIds as List<String>?,
      tags: tags == const $CopyWithPlaceholder()
          ? _value.tags
          // ignore: cast_nullable_to_non_nullable
          : tags as List<String>?,
    );
  }
}

extension $ChronicleLayerSelectorOneOf8CopyWith
    on ChronicleLayerSelectorOneOf8 {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleLayerSelectorOneOf8.copyWith(...)` or `instanceOfChronicleLayerSelectorOneOf8.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleLayerSelectorOneOf8CWProxy get copyWith =>
      _$ChronicleLayerSelectorOneOf8CWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleLayerSelectorOneOf8 _$ChronicleLayerSelectorOneOf8FromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChronicleLayerSelectorOneOf8', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['type']);
  final val = ChronicleLayerSelectorOneOf8(
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$ChronicleLayerSelectorOneOf8TypeEnumEnumMap, v),
    ),
    ruleIds: $checkedConvert(
      'ruleIds',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    tags: $checkedConvert(
      'tags',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$ChronicleLayerSelectorOneOf8ToJson(
  ChronicleLayerSelectorOneOf8 instance,
) => <String, dynamic>{
  'type': _$ChronicleLayerSelectorOneOf8TypeEnumEnumMap[instance.type]!,
  'ruleIds': ?instance.ruleIds,
  'tags': ?instance.tags,
};

const _$ChronicleLayerSelectorOneOf8TypeEnumEnumMap = {
  ChronicleLayerSelectorOneOf8TypeEnum.alerts: 'alerts',
};
