// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'field_manifest_grid.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$FieldManifestGridCWProxy {
  FieldManifestGrid nx(int nx);

  FieldManifestGrid ny(int ny);

  FieldManifestGrid lo1(num lo1);

  FieldManifestGrid la1(num la1);

  FieldManifestGrid dx(num dx);

  FieldManifestGrid dy(num dy);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FieldManifestGrid(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FieldManifestGrid(...).copyWith(id: 12, name: "My name")
  /// ```
  FieldManifestGrid call({int nx, int ny, num lo1, num la1, num dx, num dy});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfFieldManifestGrid.copyWith(...)` or call `instanceOfFieldManifestGrid.copyWith.fieldName(value)` for a single field.
class _$FieldManifestGridCWProxyImpl implements _$FieldManifestGridCWProxy {
  const _$FieldManifestGridCWProxyImpl(this._value);

  final FieldManifestGrid _value;

  @override
  FieldManifestGrid nx(int nx) => call(nx: nx);

  @override
  FieldManifestGrid ny(int ny) => call(ny: ny);

  @override
  FieldManifestGrid lo1(num lo1) => call(lo1: lo1);

  @override
  FieldManifestGrid la1(num la1) => call(la1: la1);

  @override
  FieldManifestGrid dx(num dx) => call(dx: dx);

  @override
  FieldManifestGrid dy(num dy) => call(dy: dy);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FieldManifestGrid(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FieldManifestGrid(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  FieldManifestGrid call({
    Object? nx = const $CopyWithPlaceholder(),
    Object? ny = const $CopyWithPlaceholder(),
    Object? lo1 = const $CopyWithPlaceholder(),
    Object? la1 = const $CopyWithPlaceholder(),
    Object? dx = const $CopyWithPlaceholder(),
    Object? dy = const $CopyWithPlaceholder(),
  }) {
    return FieldManifestGrid(
      nx: nx == const $CopyWithPlaceholder() || nx == null
          ? _value.nx
          // ignore: cast_nullable_to_non_nullable
          : nx as int,
      ny: ny == const $CopyWithPlaceholder() || ny == null
          ? _value.ny
          // ignore: cast_nullable_to_non_nullable
          : ny as int,
      lo1: lo1 == const $CopyWithPlaceholder() || lo1 == null
          ? _value.lo1
          // ignore: cast_nullable_to_non_nullable
          : lo1 as num,
      la1: la1 == const $CopyWithPlaceholder() || la1 == null
          ? _value.la1
          // ignore: cast_nullable_to_non_nullable
          : la1 as num,
      dx: dx == const $CopyWithPlaceholder() || dx == null
          ? _value.dx
          // ignore: cast_nullable_to_non_nullable
          : dx as num,
      dy: dy == const $CopyWithPlaceholder() || dy == null
          ? _value.dy
          // ignore: cast_nullable_to_non_nullable
          : dy as num,
    );
  }
}

extension $FieldManifestGridCopyWith on FieldManifestGrid {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfFieldManifestGrid.copyWith(...)` or `instanceOfFieldManifestGrid.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$FieldManifestGridCWProxy get copyWith =>
      _$FieldManifestGridCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

FieldManifestGrid _$FieldManifestGridFromJson(Map<String, dynamic> json) =>
    $checkedCreate('FieldManifestGrid', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['nx', 'ny', 'lo1', 'la1', 'dx', 'dy'],
      );
      final val = FieldManifestGrid(
        nx: $checkedConvert('nx', (v) => (v as num).toInt()),
        ny: $checkedConvert('ny', (v) => (v as num).toInt()),
        lo1: $checkedConvert('lo1', (v) => v as num),
        la1: $checkedConvert('la1', (v) => v as num),
        dx: $checkedConvert('dx', (v) => v as num),
        dy: $checkedConvert('dy', (v) => v as num),
      );
      return val;
    });

Map<String, dynamic> _$FieldManifestGridToJson(FieldManifestGrid instance) =>
    <String, dynamic>{
      'nx': instance.nx,
      'ny': instance.ny,
      'lo1': instance.lo1,
      'la1': instance.la1,
      'dx': instance.dx,
      'dy': instance.dy,
    };
