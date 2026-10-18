// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_patch.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChroniclePatchCWProxy {
  ChroniclePatch name(String? name);

  ChroniclePatch description(String? description);

  ChroniclePatch visibility(Visibility? visibility);

  ChroniclePatch tlp(Tlp? tlp);

  ChroniclePatch area(ChronicleArea? area);

  ChroniclePatch retention(String? retention);

  ChroniclePatch sizeCapBytes(int? sizeCapBytes);

  ChroniclePatch situationIds(List<String>? situationIds);

  ChroniclePatch hypothesisSetIds(List<String>? hypothesisSetIds);

  ChroniclePatch investigationIds(List<String>? investigationIds);

  ChroniclePatch topicIds(List<String>? topicIds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChroniclePatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChroniclePatch(...).copyWith(id: 12, name: "My name")
  /// ```
  ChroniclePatch call({
    String? name,
    String? description,
    Visibility? visibility,
    Tlp? tlp,
    ChronicleArea? area,
    String? retention,
    int? sizeCapBytes,
    List<String>? situationIds,
    List<String>? hypothesisSetIds,
    List<String>? investigationIds,
    List<String>? topicIds,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChroniclePatch.copyWith(...)` or call `instanceOfChroniclePatch.copyWith.fieldName(value)` for a single field.
class _$ChroniclePatchCWProxyImpl implements _$ChroniclePatchCWProxy {
  const _$ChroniclePatchCWProxyImpl(this._value);

  final ChroniclePatch _value;

  @override
  ChroniclePatch name(String? name) => call(name: name);

  @override
  ChroniclePatch description(String? description) =>
      call(description: description);

  @override
  ChroniclePatch visibility(Visibility? visibility) =>
      call(visibility: visibility);

  @override
  ChroniclePatch tlp(Tlp? tlp) => call(tlp: tlp);

  @override
  ChroniclePatch area(ChronicleArea? area) => call(area: area);

  @override
  ChroniclePatch retention(String? retention) => call(retention: retention);

  @override
  ChroniclePatch sizeCapBytes(int? sizeCapBytes) =>
      call(sizeCapBytes: sizeCapBytes);

  @override
  ChroniclePatch situationIds(List<String>? situationIds) =>
      call(situationIds: situationIds);

  @override
  ChroniclePatch hypothesisSetIds(List<String>? hypothesisSetIds) =>
      call(hypothesisSetIds: hypothesisSetIds);

  @override
  ChroniclePatch investigationIds(List<String>? investigationIds) =>
      call(investigationIds: investigationIds);

  @override
  ChroniclePatch topicIds(List<String>? topicIds) => call(topicIds: topicIds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChroniclePatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChroniclePatch(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChroniclePatch call({
    Object? name = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? visibility = const $CopyWithPlaceholder(),
    Object? tlp = const $CopyWithPlaceholder(),
    Object? area = const $CopyWithPlaceholder(),
    Object? retention = const $CopyWithPlaceholder(),
    Object? sizeCapBytes = const $CopyWithPlaceholder(),
    Object? situationIds = const $CopyWithPlaceholder(),
    Object? hypothesisSetIds = const $CopyWithPlaceholder(),
    Object? investigationIds = const $CopyWithPlaceholder(),
    Object? topicIds = const $CopyWithPlaceholder(),
  }) {
    return ChroniclePatch(
      name: name == const $CopyWithPlaceholder()
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String?,
      description: description == const $CopyWithPlaceholder()
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String?,
      visibility: visibility == const $CopyWithPlaceholder()
          ? _value.visibility
          // ignore: cast_nullable_to_non_nullable
          : visibility as Visibility?,
      tlp: tlp == const $CopyWithPlaceholder()
          ? _value.tlp
          // ignore: cast_nullable_to_non_nullable
          : tlp as Tlp?,
      area: area == const $CopyWithPlaceholder()
          ? _value.area
          // ignore: cast_nullable_to_non_nullable
          : area as ChronicleArea?,
      retention: retention == const $CopyWithPlaceholder()
          ? _value.retention
          // ignore: cast_nullable_to_non_nullable
          : retention as String?,
      sizeCapBytes: sizeCapBytes == const $CopyWithPlaceholder()
          ? _value.sizeCapBytes
          // ignore: cast_nullable_to_non_nullable
          : sizeCapBytes as int?,
      situationIds: situationIds == const $CopyWithPlaceholder()
          ? _value.situationIds
          // ignore: cast_nullable_to_non_nullable
          : situationIds as List<String>?,
      hypothesisSetIds: hypothesisSetIds == const $CopyWithPlaceholder()
          ? _value.hypothesisSetIds
          // ignore: cast_nullable_to_non_nullable
          : hypothesisSetIds as List<String>?,
      investigationIds: investigationIds == const $CopyWithPlaceholder()
          ? _value.investigationIds
          // ignore: cast_nullable_to_non_nullable
          : investigationIds as List<String>?,
      topicIds: topicIds == const $CopyWithPlaceholder()
          ? _value.topicIds
          // ignore: cast_nullable_to_non_nullable
          : topicIds as List<String>?,
    );
  }
}

extension $ChroniclePatchCopyWith on ChroniclePatch {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChroniclePatch.copyWith(...)` or `instanceOfChroniclePatch.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChroniclePatchCWProxy get copyWith => _$ChroniclePatchCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChroniclePatch _$ChroniclePatchFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChroniclePatch', json, ($checkedConvert) {
  final val = ChroniclePatch(
    name: $checkedConvert('name', (v) => v as String?),
    description: $checkedConvert('description', (v) => v as String?),
    visibility: $checkedConvert(
      'visibility',
      (v) => $enumDecodeNullable(_$VisibilityEnumMap, v),
    ),
    tlp: $checkedConvert('tlp', (v) => $enumDecodeNullable(_$TlpEnumMap, v)),
    area: $checkedConvert(
      'area',
      (v) =>
          v == null ? null : ChronicleArea.fromJson(v as Map<String, dynamic>),
    ),
    retention: $checkedConvert('retention', (v) => v as String?),
    sizeCapBytes: $checkedConvert('sizeCapBytes', (v) => (v as num?)?.toInt()),
    situationIds: $checkedConvert(
      'situationIds',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    hypothesisSetIds: $checkedConvert(
      'hypothesisSetIds',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    investigationIds: $checkedConvert(
      'investigationIds',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    topicIds: $checkedConvert(
      'topicIds',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$ChroniclePatchToJson(ChroniclePatch instance) =>
    <String, dynamic>{
      'name': ?instance.name,
      'description': ?instance.description,
      'visibility': ?_$VisibilityEnumMap[instance.visibility],
      'tlp': ?_$TlpEnumMap[instance.tlp],
      'area': ?instance.area?.toJson(),
      'retention': ?instance.retention,
      'sizeCapBytes': ?instance.sizeCapBytes,
      'situationIds': ?instance.situationIds,
      'hypothesisSetIds': ?instance.hypothesisSetIds,
      'investigationIds': ?instance.investigationIds,
      'topicIds': ?instance.topicIds,
    };

const _$VisibilityEnumMap = {
  Visibility.private: 'private',
  Visibility.shared: 'shared',
  Visibility.instance: 'instance',
};

const _$TlpEnumMap = {
  Tlp.CLEAR: 'CLEAR',
  Tlp.GREEN: 'GREEN',
  Tlp.AMBER: 'AMBER',
  Tlp.aMBERPlusSTRICT: 'AMBER+STRICT',
  Tlp.RED: 'RED',
};
