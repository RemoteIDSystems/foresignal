// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'report_patch.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReportPatchCWProxy {
  ReportPatch title(String? title);

  ReportPatch subtitle(String? subtitle);

  ReportPatch status(ReportPatchStatusEnum? status);

  ReportPatch authors(List<String>? authors);

  ReportPatch tlp(Tlp? tlp);

  ReportPatch visibility(Visibility? visibility);

  ReportPatch timeWindow(TimeRange? timeWindow);

  ReportPatch area(Geometry? area);

  ReportPatch blockOrder(List<String>? blockOrder);

  ReportPatch version(int? version);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportPatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportPatch(...).copyWith(id: 12, name: "My name")
  /// ```
  ReportPatch call({
    String? title,
    String? subtitle,
    ReportPatchStatusEnum? status,
    List<String>? authors,
    Tlp? tlp,
    Visibility? visibility,
    TimeRange? timeWindow,
    Geometry? area,
    List<String>? blockOrder,
    int? version,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReportPatch.copyWith(...)` or call `instanceOfReportPatch.copyWith.fieldName(value)` for a single field.
class _$ReportPatchCWProxyImpl implements _$ReportPatchCWProxy {
  const _$ReportPatchCWProxyImpl(this._value);

  final ReportPatch _value;

  @override
  ReportPatch title(String? title) => call(title: title);

  @override
  ReportPatch subtitle(String? subtitle) => call(subtitle: subtitle);

  @override
  ReportPatch status(ReportPatchStatusEnum? status) => call(status: status);

  @override
  ReportPatch authors(List<String>? authors) => call(authors: authors);

  @override
  ReportPatch tlp(Tlp? tlp) => call(tlp: tlp);

  @override
  ReportPatch visibility(Visibility? visibility) =>
      call(visibility: visibility);

  @override
  ReportPatch timeWindow(TimeRange? timeWindow) => call(timeWindow: timeWindow);

  @override
  ReportPatch area(Geometry? area) => call(area: area);

  @override
  ReportPatch blockOrder(List<String>? blockOrder) =>
      call(blockOrder: blockOrder);

  @override
  ReportPatch version(int? version) => call(version: version);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportPatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportPatch(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ReportPatch call({
    Object? title = const $CopyWithPlaceholder(),
    Object? subtitle = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? authors = const $CopyWithPlaceholder(),
    Object? tlp = const $CopyWithPlaceholder(),
    Object? visibility = const $CopyWithPlaceholder(),
    Object? timeWindow = const $CopyWithPlaceholder(),
    Object? area = const $CopyWithPlaceholder(),
    Object? blockOrder = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
  }) {
    return ReportPatch(
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      subtitle: subtitle == const $CopyWithPlaceholder()
          ? _value.subtitle
          // ignore: cast_nullable_to_non_nullable
          : subtitle as String?,
      status: status == const $CopyWithPlaceholder()
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as ReportPatchStatusEnum?,
      authors: authors == const $CopyWithPlaceholder()
          ? _value.authors
          // ignore: cast_nullable_to_non_nullable
          : authors as List<String>?,
      tlp: tlp == const $CopyWithPlaceholder()
          ? _value.tlp
          // ignore: cast_nullable_to_non_nullable
          : tlp as Tlp?,
      visibility: visibility == const $CopyWithPlaceholder()
          ? _value.visibility
          // ignore: cast_nullable_to_non_nullable
          : visibility as Visibility?,
      timeWindow: timeWindow == const $CopyWithPlaceholder()
          ? _value.timeWindow
          // ignore: cast_nullable_to_non_nullable
          : timeWindow as TimeRange?,
      area: area == const $CopyWithPlaceholder()
          ? _value.area
          // ignore: cast_nullable_to_non_nullable
          : area as Geometry?,
      blockOrder: blockOrder == const $CopyWithPlaceholder()
          ? _value.blockOrder
          // ignore: cast_nullable_to_non_nullable
          : blockOrder as List<String>?,
      version: version == const $CopyWithPlaceholder()
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as int?,
    );
  }
}

extension $ReportPatchCopyWith on ReportPatch {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReportPatch.copyWith(...)` or `instanceOfReportPatch.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReportPatchCWProxy get copyWith => _$ReportPatchCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReportPatch _$ReportPatchFromJson(Map<String, dynamic> json) => $checkedCreate(
  'ReportPatch',
  json,
  ($checkedConvert) {
    final val = ReportPatch(
      title: $checkedConvert('title', (v) => v as String?),
      subtitle: $checkedConvert('subtitle', (v) => v as String?),
      status: $checkedConvert(
        'status',
        (v) => $enumDecodeNullable(_$ReportPatchStatusEnumEnumMap, v),
      ),
      authors: $checkedConvert(
        'authors',
        (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
      ),
      tlp: $checkedConvert('tlp', (v) => $enumDecodeNullable(_$TlpEnumMap, v)),
      visibility: $checkedConvert(
        'visibility',
        (v) => $enumDecodeNullable(_$VisibilityEnumMap, v),
      ),
      timeWindow: $checkedConvert(
        'timeWindow',
        (v) => v == null ? null : TimeRange.fromJson(v as Map<String, dynamic>),
      ),
      area: $checkedConvert(
        'area',
        (v) => v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
      ),
      blockOrder: $checkedConvert(
        'blockOrder',
        (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
      ),
      version: $checkedConvert('version', (v) => (v as num?)?.toInt()),
    );
    return val;
  },
);

Map<String, dynamic> _$ReportPatchToJson(ReportPatch instance) =>
    <String, dynamic>{
      'title': ?instance.title,
      'subtitle': ?instance.subtitle,
      'status': ?_$ReportPatchStatusEnumEnumMap[instance.status],
      'authors': ?instance.authors,
      'tlp': ?_$TlpEnumMap[instance.tlp],
      'visibility': ?_$VisibilityEnumMap[instance.visibility],
      'timeWindow': ?instance.timeWindow?.toJson(),
      'area': ?instance.area?.toJson(),
      'blockOrder': ?instance.blockOrder,
      'version': ?instance.version,
    };

const _$ReportPatchStatusEnumEnumMap = {
  ReportPatchStatusEnum.draft: 'draft',
  ReportPatchStatusEnum.review: 'review',
};

const _$TlpEnumMap = {
  Tlp.CLEAR: 'CLEAR',
  Tlp.GREEN: 'GREEN',
  Tlp.AMBER: 'AMBER',
  Tlp.aMBERPlusSTRICT: 'AMBER+STRICT',
  Tlp.RED: 'RED',
};

const _$VisibilityEnumMap = {
  Visibility.private: 'private',
  Visibility.shared: 'shared',
  Visibility.instance: 'instance',
};
