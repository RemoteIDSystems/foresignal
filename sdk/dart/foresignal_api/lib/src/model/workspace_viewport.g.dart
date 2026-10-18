// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'workspace_viewport.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$WorkspaceViewportCWProxy {
  WorkspaceViewport bbox(List<num>? bbox);

  WorkspaceViewport center(List<num>? center);

  WorkspaceViewport radiusKm(num? radiusKm);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WorkspaceViewport(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WorkspaceViewport(...).copyWith(id: 12, name: "My name")
  /// ```
  WorkspaceViewport call({List<num>? bbox, List<num>? center, num? radiusKm});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfWorkspaceViewport.copyWith(...)` or call `instanceOfWorkspaceViewport.copyWith.fieldName(value)` for a single field.
class _$WorkspaceViewportCWProxyImpl implements _$WorkspaceViewportCWProxy {
  const _$WorkspaceViewportCWProxyImpl(this._value);

  final WorkspaceViewport _value;

  @override
  WorkspaceViewport bbox(List<num>? bbox) => call(bbox: bbox);

  @override
  WorkspaceViewport center(List<num>? center) => call(center: center);

  @override
  WorkspaceViewport radiusKm(num? radiusKm) => call(radiusKm: radiusKm);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WorkspaceViewport(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WorkspaceViewport(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  WorkspaceViewport call({
    Object? bbox = const $CopyWithPlaceholder(),
    Object? center = const $CopyWithPlaceholder(),
    Object? radiusKm = const $CopyWithPlaceholder(),
  }) {
    return WorkspaceViewport(
      bbox: bbox == const $CopyWithPlaceholder()
          ? _value.bbox
          // ignore: cast_nullable_to_non_nullable
          : bbox as List<num>?,
      center: center == const $CopyWithPlaceholder()
          ? _value.center
          // ignore: cast_nullable_to_non_nullable
          : center as List<num>?,
      radiusKm: radiusKm == const $CopyWithPlaceholder()
          ? _value.radiusKm
          // ignore: cast_nullable_to_non_nullable
          : radiusKm as num?,
    );
  }
}

extension $WorkspaceViewportCopyWith on WorkspaceViewport {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfWorkspaceViewport.copyWith(...)` or `instanceOfWorkspaceViewport.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$WorkspaceViewportCWProxy get copyWith =>
      _$WorkspaceViewportCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

WorkspaceViewport _$WorkspaceViewportFromJson(Map<String, dynamic> json) =>
    $checkedCreate('WorkspaceViewport', json, ($checkedConvert) {
      final val = WorkspaceViewport(
        bbox: $checkedConvert(
          'bbox',
          (v) => (v as List<dynamic>?)?.map((e) => e as num).toList(),
        ),
        center: $checkedConvert(
          'center',
          (v) => (v as List<dynamic>?)?.map((e) => e as num).toList(),
        ),
        radiusKm: $checkedConvert('radiusKm', (v) => v as num?),
      );
      return val;
    });

Map<String, dynamic> _$WorkspaceViewportToJson(WorkspaceViewport instance) =>
    <String, dynamic>{
      'bbox': ?instance.bbox,
      'center': ?instance.center,
      'radiusKm': ?instance.radiusKm,
    };
