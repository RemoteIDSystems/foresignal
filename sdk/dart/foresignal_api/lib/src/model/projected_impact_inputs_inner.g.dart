// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'projected_impact_inputs_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ProjectedImpactInputsInnerCWProxy {
  ProjectedImpactInputsInner name(String name);

  ProjectedImpactInputsInner value(Object? value);

  ProjectedImpactInputsInner ref(String? ref);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProjectedImpactInputsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProjectedImpactInputsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ProjectedImpactInputsInner call({String name, Object? value, String? ref});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfProjectedImpactInputsInner.copyWith(...)` or call `instanceOfProjectedImpactInputsInner.copyWith.fieldName(value)` for a single field.
class _$ProjectedImpactInputsInnerCWProxyImpl
    implements _$ProjectedImpactInputsInnerCWProxy {
  const _$ProjectedImpactInputsInnerCWProxyImpl(this._value);

  final ProjectedImpactInputsInner _value;

  @override
  ProjectedImpactInputsInner name(String name) => call(name: name);

  @override
  ProjectedImpactInputsInner value(Object? value) => call(value: value);

  @override
  ProjectedImpactInputsInner ref(String? ref) => call(ref: ref);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProjectedImpactInputsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProjectedImpactInputsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ProjectedImpactInputsInner call({
    Object? name = const $CopyWithPlaceholder(),
    Object? value = const $CopyWithPlaceholder(),
    Object? ref = const $CopyWithPlaceholder(),
  }) {
    return ProjectedImpactInputsInner(
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      value: value == const $CopyWithPlaceholder()
          ? _value.value
          // ignore: cast_nullable_to_non_nullable
          : value as Object?,
      ref: ref == const $CopyWithPlaceholder()
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String?,
    );
  }
}

extension $ProjectedImpactInputsInnerCopyWith on ProjectedImpactInputsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfProjectedImpactInputsInner.copyWith(...)` or `instanceOfProjectedImpactInputsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ProjectedImpactInputsInnerCWProxy get copyWith =>
      _$ProjectedImpactInputsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ProjectedImpactInputsInner _$ProjectedImpactInputsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ProjectedImpactInputsInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['name']);
  final val = ProjectedImpactInputsInner(
    name: $checkedConvert('name', (v) => v as String),
    value: $checkedConvert('value', (v) => v),
    ref: $checkedConvert('ref', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$ProjectedImpactInputsInnerToJson(
  ProjectedImpactInputsInner instance,
) => <String, dynamic>{
  'name': instance.name,
  'value': ?instance.value,
  'ref': ?instance.ref,
};
