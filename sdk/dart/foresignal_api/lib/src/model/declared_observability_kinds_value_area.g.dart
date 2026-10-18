// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'declared_observability_kinds_value_area.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DeclaredObservabilityKindsValueAreaCWProxy {
  DeclaredObservabilityKindsValueArea type(
    DeclaredObservabilityKindsValueAreaTypeEnum type,
  );

  DeclaredObservabilityKindsValueArea bbox(List<num>? bbox);

  DeclaredObservabilityKindsValueArea regionCodes(List<String>? regionCodes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DeclaredObservabilityKindsValueArea(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DeclaredObservabilityKindsValueArea(...).copyWith(id: 12, name: "My name")
  /// ```
  DeclaredObservabilityKindsValueArea call({
    DeclaredObservabilityKindsValueAreaTypeEnum type,
    List<num>? bbox,
    List<String>? regionCodes,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDeclaredObservabilityKindsValueArea.copyWith(...)` or call `instanceOfDeclaredObservabilityKindsValueArea.copyWith.fieldName(value)` for a single field.
class _$DeclaredObservabilityKindsValueAreaCWProxyImpl
    implements _$DeclaredObservabilityKindsValueAreaCWProxy {
  const _$DeclaredObservabilityKindsValueAreaCWProxyImpl(this._value);

  final DeclaredObservabilityKindsValueArea _value;

  @override
  DeclaredObservabilityKindsValueArea type(
    DeclaredObservabilityKindsValueAreaTypeEnum type,
  ) => call(type: type);

  @override
  DeclaredObservabilityKindsValueArea bbox(List<num>? bbox) => call(bbox: bbox);

  @override
  DeclaredObservabilityKindsValueArea regionCodes(List<String>? regionCodes) =>
      call(regionCodes: regionCodes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DeclaredObservabilityKindsValueArea(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DeclaredObservabilityKindsValueArea(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DeclaredObservabilityKindsValueArea call({
    Object? type = const $CopyWithPlaceholder(),
    Object? bbox = const $CopyWithPlaceholder(),
    Object? regionCodes = const $CopyWithPlaceholder(),
  }) {
    return DeclaredObservabilityKindsValueArea(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as DeclaredObservabilityKindsValueAreaTypeEnum,
      bbox: bbox == const $CopyWithPlaceholder()
          ? _value.bbox
          // ignore: cast_nullable_to_non_nullable
          : bbox as List<num>?,
      regionCodes: regionCodes == const $CopyWithPlaceholder()
          ? _value.regionCodes
          // ignore: cast_nullable_to_non_nullable
          : regionCodes as List<String>?,
    );
  }
}

extension $DeclaredObservabilityKindsValueAreaCopyWith
    on DeclaredObservabilityKindsValueArea {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDeclaredObservabilityKindsValueArea.copyWith(...)` or `instanceOfDeclaredObservabilityKindsValueArea.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DeclaredObservabilityKindsValueAreaCWProxy get copyWith =>
      _$DeclaredObservabilityKindsValueAreaCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DeclaredObservabilityKindsValueArea
_$DeclaredObservabilityKindsValueAreaFromJson(Map<String, dynamic> json) =>
    $checkedCreate('DeclaredObservabilityKindsValueArea', json, (
      $checkedConvert,
    ) {
      $checkKeys(json, requiredKeys: const ['type']);
      final val = DeclaredObservabilityKindsValueArea(
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(
            _$DeclaredObservabilityKindsValueAreaTypeEnumEnumMap,
            v,
          ),
        ),
        bbox: $checkedConvert(
          'bbox',
          (v) => (v as List<dynamic>?)?.map((e) => e as num).toList(),
        ),
        regionCodes: $checkedConvert(
          'regionCodes',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$DeclaredObservabilityKindsValueAreaToJson(
  DeclaredObservabilityKindsValueArea instance,
) => <String, dynamic>{
  'type': _$DeclaredObservabilityKindsValueAreaTypeEnumEnumMap[instance.type]!,
  'bbox': ?instance.bbox,
  'regionCodes': ?instance.regionCodes,
};

const _$DeclaredObservabilityKindsValueAreaTypeEnumEnumMap = {
  DeclaredObservabilityKindsValueAreaTypeEnum.global: 'global',
  DeclaredObservabilityKindsValueAreaTypeEnum.bbox: 'bbox',
  DeclaredObservabilityKindsValueAreaTypeEnum.regions: 'regions',
};
