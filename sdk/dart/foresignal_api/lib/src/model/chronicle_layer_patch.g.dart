// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_layer_patch.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleLayerPatchCWProxy {
  ChronicleLayerPatch name(String? name);

  ChronicleLayerPatch selector(ChronicleLayerSelector? selector);

  ChronicleLayerPatch areaOverride(ChronicleArea? areaOverride);

  ChronicleLayerPatch capture(CaptureMode? capture);

  ChronicleLayerPatch enabled(bool? enabled);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleLayerPatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleLayerPatch(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleLayerPatch call({
    String? name,
    ChronicleLayerSelector? selector,
    ChronicleArea? areaOverride,
    CaptureMode? capture,
    bool? enabled,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleLayerPatch.copyWith(...)` or call `instanceOfChronicleLayerPatch.copyWith.fieldName(value)` for a single field.
class _$ChronicleLayerPatchCWProxyImpl implements _$ChronicleLayerPatchCWProxy {
  const _$ChronicleLayerPatchCWProxyImpl(this._value);

  final ChronicleLayerPatch _value;

  @override
  ChronicleLayerPatch name(String? name) => call(name: name);

  @override
  ChronicleLayerPatch selector(ChronicleLayerSelector? selector) =>
      call(selector: selector);

  @override
  ChronicleLayerPatch areaOverride(ChronicleArea? areaOverride) =>
      call(areaOverride: areaOverride);

  @override
  ChronicleLayerPatch capture(CaptureMode? capture) => call(capture: capture);

  @override
  ChronicleLayerPatch enabled(bool? enabled) => call(enabled: enabled);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleLayerPatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleLayerPatch(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleLayerPatch call({
    Object? name = const $CopyWithPlaceholder(),
    Object? selector = const $CopyWithPlaceholder(),
    Object? areaOverride = const $CopyWithPlaceholder(),
    Object? capture = const $CopyWithPlaceholder(),
    Object? enabled = const $CopyWithPlaceholder(),
  }) {
    return ChronicleLayerPatch(
      name: name == const $CopyWithPlaceholder()
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String?,
      selector: selector == const $CopyWithPlaceholder()
          ? _value.selector
          // ignore: cast_nullable_to_non_nullable
          : selector as ChronicleLayerSelector?,
      areaOverride: areaOverride == const $CopyWithPlaceholder()
          ? _value.areaOverride
          // ignore: cast_nullable_to_non_nullable
          : areaOverride as ChronicleArea?,
      capture: capture == const $CopyWithPlaceholder()
          ? _value.capture
          // ignore: cast_nullable_to_non_nullable
          : capture as CaptureMode?,
      enabled: enabled == const $CopyWithPlaceholder()
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool?,
    );
  }
}

extension $ChronicleLayerPatchCopyWith on ChronicleLayerPatch {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleLayerPatch.copyWith(...)` or `instanceOfChronicleLayerPatch.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleLayerPatchCWProxy get copyWith =>
      _$ChronicleLayerPatchCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleLayerPatch _$ChronicleLayerPatchFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChronicleLayerPatch', json, ($checkedConvert) {
  final val = ChronicleLayerPatch(
    name: $checkedConvert('name', (v) => v as String?),
    selector: $checkedConvert(
      'selector',
      (v) => v == null
          ? null
          : ChronicleLayerSelector.fromJson(v as Map<String, dynamic>),
    ),
    areaOverride: $checkedConvert(
      'areaOverride',
      (v) =>
          v == null ? null : ChronicleArea.fromJson(v as Map<String, dynamic>),
    ),
    capture: $checkedConvert(
      'capture',
      (v) => v == null ? null : CaptureMode.fromJson(v as Map<String, dynamic>),
    ),
    enabled: $checkedConvert('enabled', (v) => v as bool?),
  );
  return val;
});

Map<String, dynamic> _$ChronicleLayerPatchToJson(
  ChronicleLayerPatch instance,
) => <String, dynamic>{
  'name': ?instance.name,
  'selector': ?instance.selector?.toJson(),
  'areaOverride': ?instance.areaOverride?.toJson(),
  'capture': ?instance.capture?.toJson(),
  'enabled': ?instance.enabled,
};
