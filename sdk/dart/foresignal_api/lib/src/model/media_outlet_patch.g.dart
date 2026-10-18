// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'media_outlet_patch.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MediaOutletPatchCWProxy {
  MediaOutletPatch name(String? name);

  MediaOutletPatch type(MediaOutletPatchTypeEnum? type);

  MediaOutletPatch ownerOrg(String? ownerOrg);

  MediaOutletPatch parentOrg(String? parentOrg);

  MediaOutletPatch wikidataQid(String? wikidataQid);

  MediaOutletPatch domains(List<String>? domains);

  MediaOutletPatch mergeOutletIds(List<String>? mergeOutletIds);

  MediaOutletPatch aggregates(List<String>? aggregates);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MediaOutletPatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MediaOutletPatch(...).copyWith(id: 12, name: "My name")
  /// ```
  MediaOutletPatch call({
    String? name,
    MediaOutletPatchTypeEnum? type,
    String? ownerOrg,
    String? parentOrg,
    String? wikidataQid,
    List<String>? domains,
    List<String>? mergeOutletIds,
    List<String>? aggregates,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMediaOutletPatch.copyWith(...)` or call `instanceOfMediaOutletPatch.copyWith.fieldName(value)` for a single field.
class _$MediaOutletPatchCWProxyImpl implements _$MediaOutletPatchCWProxy {
  const _$MediaOutletPatchCWProxyImpl(this._value);

  final MediaOutletPatch _value;

  @override
  MediaOutletPatch name(String? name) => call(name: name);

  @override
  MediaOutletPatch type(MediaOutletPatchTypeEnum? type) => call(type: type);

  @override
  MediaOutletPatch ownerOrg(String? ownerOrg) => call(ownerOrg: ownerOrg);

  @override
  MediaOutletPatch parentOrg(String? parentOrg) => call(parentOrg: parentOrg);

  @override
  MediaOutletPatch wikidataQid(String? wikidataQid) =>
      call(wikidataQid: wikidataQid);

  @override
  MediaOutletPatch domains(List<String>? domains) => call(domains: domains);

  @override
  MediaOutletPatch mergeOutletIds(List<String>? mergeOutletIds) =>
      call(mergeOutletIds: mergeOutletIds);

  @override
  MediaOutletPatch aggregates(List<String>? aggregates) =>
      call(aggregates: aggregates);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MediaOutletPatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MediaOutletPatch(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MediaOutletPatch call({
    Object? name = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? ownerOrg = const $CopyWithPlaceholder(),
    Object? parentOrg = const $CopyWithPlaceholder(),
    Object? wikidataQid = const $CopyWithPlaceholder(),
    Object? domains = const $CopyWithPlaceholder(),
    Object? mergeOutletIds = const $CopyWithPlaceholder(),
    Object? aggregates = const $CopyWithPlaceholder(),
  }) {
    return MediaOutletPatch(
      name: name == const $CopyWithPlaceholder()
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String?,
      type: type == const $CopyWithPlaceholder()
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as MediaOutletPatchTypeEnum?,
      ownerOrg: ownerOrg == const $CopyWithPlaceholder()
          ? _value.ownerOrg
          // ignore: cast_nullable_to_non_nullable
          : ownerOrg as String?,
      parentOrg: parentOrg == const $CopyWithPlaceholder()
          ? _value.parentOrg
          // ignore: cast_nullable_to_non_nullable
          : parentOrg as String?,
      wikidataQid: wikidataQid == const $CopyWithPlaceholder()
          ? _value.wikidataQid
          // ignore: cast_nullable_to_non_nullable
          : wikidataQid as String?,
      domains: domains == const $CopyWithPlaceholder()
          ? _value.domains
          // ignore: cast_nullable_to_non_nullable
          : domains as List<String>?,
      mergeOutletIds: mergeOutletIds == const $CopyWithPlaceholder()
          ? _value.mergeOutletIds
          // ignore: cast_nullable_to_non_nullable
          : mergeOutletIds as List<String>?,
      aggregates: aggregates == const $CopyWithPlaceholder()
          ? _value.aggregates
          // ignore: cast_nullable_to_non_nullable
          : aggregates as List<String>?,
    );
  }
}

extension $MediaOutletPatchCopyWith on MediaOutletPatch {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMediaOutletPatch.copyWith(...)` or `instanceOfMediaOutletPatch.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MediaOutletPatchCWProxy get copyWith => _$MediaOutletPatchCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MediaOutletPatch _$MediaOutletPatchFromJson(Map<String, dynamic> json) =>
    $checkedCreate('MediaOutletPatch', json, ($checkedConvert) {
      final val = MediaOutletPatch(
        name: $checkedConvert('name', (v) => v as String?),
        type: $checkedConvert(
          'type',
          (v) => $enumDecodeNullable(_$MediaOutletPatchTypeEnumEnumMap, v),
        ),
        ownerOrg: $checkedConvert('ownerOrg', (v) => v as String?),
        parentOrg: $checkedConvert('parentOrg', (v) => v as String?),
        wikidataQid: $checkedConvert('wikidataQid', (v) => v as String?),
        domains: $checkedConvert(
          'domains',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        mergeOutletIds: $checkedConvert(
          'mergeOutletIds',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        aggregates: $checkedConvert(
          'aggregates',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$MediaOutletPatchToJson(MediaOutletPatch instance) =>
    <String, dynamic>{
      'name': ?instance.name,
      'type': ?_$MediaOutletPatchTypeEnumEnumMap[instance.type],
      'ownerOrg': ?instance.ownerOrg,
      'parentOrg': ?instance.parentOrg,
      'wikidataQid': ?instance.wikidataQid,
      'domains': ?instance.domains,
      'mergeOutletIds': ?instance.mergeOutletIds,
      'aggregates': ?instance.aggregates,
    };

const _$MediaOutletPatchTypeEnumEnumMap = {
  MediaOutletPatchTypeEnum.wire: 'wire',
  MediaOutletPatchTypeEnum.national: 'national',
  MediaOutletPatchTypeEnum.regional: 'regional',
  MediaOutletPatchTypeEnum.local: 'local',
  MediaOutletPatchTypeEnum.publicBroadcaster: 'public-broadcaster',
  MediaOutletPatchTypeEnum.stateAffiliated: 'state-affiliated',
  MediaOutletPatchTypeEnum.aggregator: 'aggregator',
  MediaOutletPatchTypeEnum.blog: 'blog',
  MediaOutletPatchTypeEnum.official: 'official',
  MediaOutletPatchTypeEnum.other: 'other',
};
