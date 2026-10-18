// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_layer_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleLayerInputCWProxy {
  ChronicleLayerInput name(String name);

  ChronicleLayerInput selector(ChronicleLayerSelector selector);

  ChronicleLayerInput areaOverride(ChronicleArea? areaOverride);

  ChronicleLayerInput capture(CaptureMode capture);

  ChronicleLayerInput enabled(bool? enabled);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleLayerInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleLayerInput(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleLayerInput call({
    String name,
    ChronicleLayerSelector selector,
    ChronicleArea? areaOverride,
    CaptureMode capture,
    bool? enabled,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleLayerInput.copyWith(...)` or call `instanceOfChronicleLayerInput.copyWith.fieldName(value)` for a single field.
class _$ChronicleLayerInputCWProxyImpl implements _$ChronicleLayerInputCWProxy {
  const _$ChronicleLayerInputCWProxyImpl(this._value);

  final ChronicleLayerInput _value;

  @override
  ChronicleLayerInput name(String name) => call(name: name);

  @override
  ChronicleLayerInput selector(ChronicleLayerSelector selector) =>
      call(selector: selector);

  @override
  ChronicleLayerInput areaOverride(ChronicleArea? areaOverride) =>
      call(areaOverride: areaOverride);

  @override
  ChronicleLayerInput capture(CaptureMode capture) => call(capture: capture);

  @override
  ChronicleLayerInput enabled(bool? enabled) => call(enabled: enabled);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleLayerInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleLayerInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleLayerInput call({
    Object? name = const $CopyWithPlaceholder(),
    Object? selector = const $CopyWithPlaceholder(),
    Object? areaOverride = const $CopyWithPlaceholder(),
    Object? capture = const $CopyWithPlaceholder(),
    Object? enabled = const $CopyWithPlaceholder(),
  }) {
    return ChronicleLayerInput(
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      selector: selector == const $CopyWithPlaceholder() || selector == null
          ? _value.selector
          // ignore: cast_nullable_to_non_nullable
          : selector as ChronicleLayerSelector,
      areaOverride: areaOverride == const $CopyWithPlaceholder()
          ? _value.areaOverride
          // ignore: cast_nullable_to_non_nullable
          : areaOverride as ChronicleArea?,
      capture: capture == const $CopyWithPlaceholder() || capture == null
          ? _value.capture
          // ignore: cast_nullable_to_non_nullable
          : capture as CaptureMode,
      enabled: enabled == const $CopyWithPlaceholder()
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool?,
    );
  }
}

extension $ChronicleLayerInputCopyWith on ChronicleLayerInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleLayerInput.copyWith(...)` or `instanceOfChronicleLayerInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleLayerInputCWProxy get copyWith =>
      _$ChronicleLayerInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleLayerInput _$ChronicleLayerInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ChronicleLayerInput', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['name', 'selector', 'capture']);
      final val = ChronicleLayerInput(
        name: $checkedConvert('name', (v) => v as String),
        selector: $checkedConvert(
          'selector',
          (v) => ChronicleLayerSelector.fromJson(v as Map<String, dynamic>),
        ),
        areaOverride: $checkedConvert(
          'areaOverride',
          (v) => v == null
              ? null
              : ChronicleArea.fromJson(v as Map<String, dynamic>),
        ),
        capture: $checkedConvert(
          'capture',
          (v) => CaptureMode.fromJson(v as Map<String, dynamic>),
        ),
        enabled: $checkedConvert('enabled', (v) => v as bool? ?? true),
      );
      return val;
    });

Map<String, dynamic> _$ChronicleLayerInputToJson(
  ChronicleLayerInput instance,
) => <String, dynamic>{
  'name': instance.name,
  'selector': instance.selector.toJson(),
  'areaOverride': ?instance.areaOverride?.toJson(),
  'capture': instance.capture.toJson(),
  'enabled': ?instance.enabled,
};
