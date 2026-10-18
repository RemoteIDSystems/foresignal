// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'external_binding_preview_effects_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ExternalBindingPreviewEffectsInnerCWProxy {
  ExternalBindingPreviewEffectsInner itemRef(String? itemRef);

  ExternalBindingPreviewEffectsInner action(String? action);

  ExternalBindingPreviewEffectsInner targetRef(String? targetRef);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExternalBindingPreviewEffectsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExternalBindingPreviewEffectsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ExternalBindingPreviewEffectsInner call({
    String? itemRef,
    String? action,
    String? targetRef,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfExternalBindingPreviewEffectsInner.copyWith(...)` or call `instanceOfExternalBindingPreviewEffectsInner.copyWith.fieldName(value)` for a single field.
class _$ExternalBindingPreviewEffectsInnerCWProxyImpl
    implements _$ExternalBindingPreviewEffectsInnerCWProxy {
  const _$ExternalBindingPreviewEffectsInnerCWProxyImpl(this._value);

  final ExternalBindingPreviewEffectsInner _value;

  @override
  ExternalBindingPreviewEffectsInner itemRef(String? itemRef) =>
      call(itemRef: itemRef);

  @override
  ExternalBindingPreviewEffectsInner action(String? action) =>
      call(action: action);

  @override
  ExternalBindingPreviewEffectsInner targetRef(String? targetRef) =>
      call(targetRef: targetRef);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExternalBindingPreviewEffectsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExternalBindingPreviewEffectsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ExternalBindingPreviewEffectsInner call({
    Object? itemRef = const $CopyWithPlaceholder(),
    Object? action = const $CopyWithPlaceholder(),
    Object? targetRef = const $CopyWithPlaceholder(),
  }) {
    return ExternalBindingPreviewEffectsInner(
      itemRef: itemRef == const $CopyWithPlaceholder()
          ? _value.itemRef
          // ignore: cast_nullable_to_non_nullable
          : itemRef as String?,
      action: action == const $CopyWithPlaceholder()
          ? _value.action
          // ignore: cast_nullable_to_non_nullable
          : action as String?,
      targetRef: targetRef == const $CopyWithPlaceholder()
          ? _value.targetRef
          // ignore: cast_nullable_to_non_nullable
          : targetRef as String?,
    );
  }
}

extension $ExternalBindingPreviewEffectsInnerCopyWith
    on ExternalBindingPreviewEffectsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfExternalBindingPreviewEffectsInner.copyWith(...)` or `instanceOfExternalBindingPreviewEffectsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ExternalBindingPreviewEffectsInnerCWProxy get copyWith =>
      _$ExternalBindingPreviewEffectsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExternalBindingPreviewEffectsInner _$ExternalBindingPreviewEffectsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ExternalBindingPreviewEffectsInner', json, (
  $checkedConvert,
) {
  final val = ExternalBindingPreviewEffectsInner(
    itemRef: $checkedConvert('itemRef', (v) => v as String?),
    action: $checkedConvert('action', (v) => v as String?),
    targetRef: $checkedConvert('targetRef', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$ExternalBindingPreviewEffectsInnerToJson(
  ExternalBindingPreviewEffectsInner instance,
) => <String, dynamic>{
  'itemRef': ?instance.itemRef,
  'action': ?instance.action,
  'targetRef': ?instance.targetRef,
};
