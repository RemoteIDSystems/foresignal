// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'velocity_json_component.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$VelocityJsonComponentCWProxy {
  VelocityJsonComponent header(VelocityJsonComponentHeader header);

  VelocityJsonComponent data(List<num?> data);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `VelocityJsonComponent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// VelocityJsonComponent(...).copyWith(id: 12, name: "My name")
  /// ```
  VelocityJsonComponent call({
    VelocityJsonComponentHeader header,
    List<num?> data,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfVelocityJsonComponent.copyWith(...)` or call `instanceOfVelocityJsonComponent.copyWith.fieldName(value)` for a single field.
class _$VelocityJsonComponentCWProxyImpl
    implements _$VelocityJsonComponentCWProxy {
  const _$VelocityJsonComponentCWProxyImpl(this._value);

  final VelocityJsonComponent _value;

  @override
  VelocityJsonComponent header(VelocityJsonComponentHeader header) =>
      call(header: header);

  @override
  VelocityJsonComponent data(List<num?> data) => call(data: data);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `VelocityJsonComponent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// VelocityJsonComponent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  VelocityJsonComponent call({
    Object? header = const $CopyWithPlaceholder(),
    Object? data = const $CopyWithPlaceholder(),
  }) {
    return VelocityJsonComponent(
      header: header == const $CopyWithPlaceholder() || header == null
          ? _value.header
          // ignore: cast_nullable_to_non_nullable
          : header as VelocityJsonComponentHeader,
      data: data == const $CopyWithPlaceholder() || data == null
          ? _value.data
          // ignore: cast_nullable_to_non_nullable
          : data as List<num?>,
    );
  }
}

extension $VelocityJsonComponentCopyWith on VelocityJsonComponent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfVelocityJsonComponent.copyWith(...)` or `instanceOfVelocityJsonComponent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$VelocityJsonComponentCWProxy get copyWith =>
      _$VelocityJsonComponentCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VelocityJsonComponent _$VelocityJsonComponentFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('VelocityJsonComponent', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['header', 'data']);
  final val = VelocityJsonComponent(
    header: $checkedConvert(
      'header',
      (v) => VelocityJsonComponentHeader.fromJson(v as Map<String, dynamic>),
    ),
    data: $checkedConvert(
      'data',
      (v) => (v as List<dynamic>).map((e) => e as num?).toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$VelocityJsonComponentToJson(
  VelocityJsonComponent instance,
) => <String, dynamic>{
  'header': instance.header.toJson(),
  'data': instance.data,
};
