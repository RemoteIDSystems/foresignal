// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'population_pack_import_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PopulationPackImportRequestCWProxy {
  PopulationPackImportRequest path(String path);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PopulationPackImportRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PopulationPackImportRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  PopulationPackImportRequest call({String path});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPopulationPackImportRequest.copyWith(...)` or call `instanceOfPopulationPackImportRequest.copyWith.fieldName(value)` for a single field.
class _$PopulationPackImportRequestCWProxyImpl
    implements _$PopulationPackImportRequestCWProxy {
  const _$PopulationPackImportRequestCWProxyImpl(this._value);

  final PopulationPackImportRequest _value;

  @override
  PopulationPackImportRequest path(String path) => call(path: path);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PopulationPackImportRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PopulationPackImportRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PopulationPackImportRequest call({
    Object? path = const $CopyWithPlaceholder(),
  }) {
    return PopulationPackImportRequest(
      path: path == const $CopyWithPlaceholder() || path == null
          ? _value.path
          // ignore: cast_nullable_to_non_nullable
          : path as String,
    );
  }
}

extension $PopulationPackImportRequestCopyWith on PopulationPackImportRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPopulationPackImportRequest.copyWith(...)` or `instanceOfPopulationPackImportRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PopulationPackImportRequestCWProxy get copyWith =>
      _$PopulationPackImportRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PopulationPackImportRequest _$PopulationPackImportRequestFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PopulationPackImportRequest', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['path']);
  final val = PopulationPackImportRequest(
    path: $checkedConvert('path', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$PopulationPackImportRequestToJson(
  PopulationPackImportRequest instance,
) => <String, dynamic>{'path': instance.path};
