// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'edp_suggested_binding.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EdpSuggestedBindingCWProxy {
  EdpSuggestedBinding target(EdpSuggestedBindingTargetEnum target);

  EdpSuggestedBinding config(Map<String, Object>? config);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpSuggestedBinding(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpSuggestedBinding(...).copyWith(id: 12, name: "My name")
  /// ```
  EdpSuggestedBinding call({
    EdpSuggestedBindingTargetEnum target,
    Map<String, Object>? config,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEdpSuggestedBinding.copyWith(...)` or call `instanceOfEdpSuggestedBinding.copyWith.fieldName(value)` for a single field.
class _$EdpSuggestedBindingCWProxyImpl implements _$EdpSuggestedBindingCWProxy {
  const _$EdpSuggestedBindingCWProxyImpl(this._value);

  final EdpSuggestedBinding _value;

  @override
  EdpSuggestedBinding target(EdpSuggestedBindingTargetEnum target) =>
      call(target: target);

  @override
  EdpSuggestedBinding config(Map<String, Object>? config) =>
      call(config: config);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpSuggestedBinding(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpSuggestedBinding(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EdpSuggestedBinding call({
    Object? target = const $CopyWithPlaceholder(),
    Object? config = const $CopyWithPlaceholder(),
  }) {
    return EdpSuggestedBinding(
      target: target == const $CopyWithPlaceholder() || target == null
          ? _value.target
          // ignore: cast_nullable_to_non_nullable
          : target as EdpSuggestedBindingTargetEnum,
      config: config == const $CopyWithPlaceholder()
          ? _value.config
          // ignore: cast_nullable_to_non_nullable
          : config as Map<String, Object>?,
    );
  }
}

extension $EdpSuggestedBindingCopyWith on EdpSuggestedBinding {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEdpSuggestedBinding.copyWith(...)` or `instanceOfEdpSuggestedBinding.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EdpSuggestedBindingCWProxy get copyWith =>
      _$EdpSuggestedBindingCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EdpSuggestedBinding _$EdpSuggestedBindingFromJson(Map<String, dynamic> json) =>
    $checkedCreate('EdpSuggestedBinding', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['target']);
      final val = EdpSuggestedBinding(
        target: $checkedConvert(
          'target',
          (v) => $enumDecode(_$EdpSuggestedBindingTargetEnumEnumMap, v),
        ),
        config: $checkedConvert(
          'config',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, e as Object),
          ),
        ),
      );
      return val;
    });

Map<String, dynamic> _$EdpSuggestedBindingToJson(
  EdpSuggestedBinding instance,
) => <String, dynamic>{
  'target': _$EdpSuggestedBindingTargetEnumEnumMap[instance.target]!,
  'config': ?instance.config,
};

const _$EdpSuggestedBindingTargetEnumEnumMap = {
  EdpSuggestedBindingTargetEnum.series: 'series',
  EdpSuggestedBindingTargetEnum.canonicalFamily: 'canonical-family',
  EdpSuggestedBindingTargetEnum.social: 'social',
  EdpSuggestedBindingTargetEnum.news: 'news',
  EdpSuggestedBindingTargetEnum.evidence: 'evidence',
  EdpSuggestedBindingTargetEnum.indicator: 'indicator',
  EdpSuggestedBindingTargetEnum.forecast: 'forecast',
  EdpSuggestedBindingTargetEnum.place: 'place',
  EdpSuggestedBindingTargetEnum.situation: 'situation',
  EdpSuggestedBindingTargetEnum.chronicle: 'chronicle',
};
