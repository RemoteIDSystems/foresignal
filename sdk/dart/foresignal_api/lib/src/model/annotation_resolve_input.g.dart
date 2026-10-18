// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'annotation_resolve_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnnotationResolveInputCWProxy {
  AnnotationResolveInput resolved(bool? resolved);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnnotationResolveInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnnotationResolveInput(...).copyWith(id: 12, name: "My name")
  /// ```
  AnnotationResolveInput call({bool? resolved});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnnotationResolveInput.copyWith(...)` or call `instanceOfAnnotationResolveInput.copyWith.fieldName(value)` for a single field.
class _$AnnotationResolveInputCWProxyImpl
    implements _$AnnotationResolveInputCWProxy {
  const _$AnnotationResolveInputCWProxyImpl(this._value);

  final AnnotationResolveInput _value;

  @override
  AnnotationResolveInput resolved(bool? resolved) => call(resolved: resolved);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnnotationResolveInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnnotationResolveInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnnotationResolveInput call({
    Object? resolved = const $CopyWithPlaceholder(),
  }) {
    return AnnotationResolveInput(
      resolved: resolved == const $CopyWithPlaceholder()
          ? _value.resolved
          // ignore: cast_nullable_to_non_nullable
          : resolved as bool?,
    );
  }
}

extension $AnnotationResolveInputCopyWith on AnnotationResolveInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnnotationResolveInput.copyWith(...)` or `instanceOfAnnotationResolveInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnnotationResolveInputCWProxy get copyWith =>
      _$AnnotationResolveInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnnotationResolveInput _$AnnotationResolveInputFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AnnotationResolveInput', json, ($checkedConvert) {
  final val = AnnotationResolveInput(
    resolved: $checkedConvert('resolved', (v) => v as bool? ?? true),
  );
  return val;
});

Map<String, dynamic> _$AnnotationResolveInputToJson(
  AnnotationResolveInput instance,
) => <String, dynamic>{'resolved': ?instance.resolved};
