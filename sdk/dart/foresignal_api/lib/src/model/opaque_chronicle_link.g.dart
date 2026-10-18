// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'opaque_chronicle_link.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OpaqueChronicleLinkCWProxy {
  OpaqueChronicleLink linkId(String linkId);

  OpaqueChronicleLink disclosure(OpaqueChronicleLinkDisclosureEnum disclosure);

  OpaqueChronicleLink chronicleId(String? chronicleId);

  OpaqueChronicleLink strength(OpaqueChronicleLinkStrengthEnum strength);

  OpaqueChronicleLink dimensions(
    List<OpaqueChronicleLinkDimensionsEnum> dimensions,
  );

  OpaqueChronicleLink timeOverlap(
    OpaqueChronicleLinkTimeOverlapEnum timeOverlap,
  );

  OpaqueChronicleLink remote(bool? remote);

  OpaqueChronicleLink ownerHandle(String? ownerHandle);

  OpaqueChronicleLink title(String? title);

  OpaqueChronicleLink accessRequestStatus(
    OpaqueChronicleLinkAccessRequestStatusEnum? accessRequestStatus,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OpaqueChronicleLink(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OpaqueChronicleLink(...).copyWith(id: 12, name: "My name")
  /// ```
  OpaqueChronicleLink call({
    String linkId,
    OpaqueChronicleLinkDisclosureEnum disclosure,
    String? chronicleId,
    OpaqueChronicleLinkStrengthEnum strength,
    List<OpaqueChronicleLinkDimensionsEnum> dimensions,
    OpaqueChronicleLinkTimeOverlapEnum timeOverlap,
    bool? remote,
    String? ownerHandle,
    String? title,
    OpaqueChronicleLinkAccessRequestStatusEnum? accessRequestStatus,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfOpaqueChronicleLink.copyWith(...)` or call `instanceOfOpaqueChronicleLink.copyWith.fieldName(value)` for a single field.
class _$OpaqueChronicleLinkCWProxyImpl implements _$OpaqueChronicleLinkCWProxy {
  const _$OpaqueChronicleLinkCWProxyImpl(this._value);

  final OpaqueChronicleLink _value;

  @override
  OpaqueChronicleLink linkId(String linkId) => call(linkId: linkId);

  @override
  OpaqueChronicleLink disclosure(
    OpaqueChronicleLinkDisclosureEnum disclosure,
  ) => call(disclosure: disclosure);

  @override
  OpaqueChronicleLink chronicleId(String? chronicleId) =>
      call(chronicleId: chronicleId);

  @override
  OpaqueChronicleLink strength(OpaqueChronicleLinkStrengthEnum strength) =>
      call(strength: strength);

  @override
  OpaqueChronicleLink dimensions(
    List<OpaqueChronicleLinkDimensionsEnum> dimensions,
  ) => call(dimensions: dimensions);

  @override
  OpaqueChronicleLink timeOverlap(
    OpaqueChronicleLinkTimeOverlapEnum timeOverlap,
  ) => call(timeOverlap: timeOverlap);

  @override
  OpaqueChronicleLink remote(bool? remote) => call(remote: remote);

  @override
  OpaqueChronicleLink ownerHandle(String? ownerHandle) =>
      call(ownerHandle: ownerHandle);

  @override
  OpaqueChronicleLink title(String? title) => call(title: title);

  @override
  OpaqueChronicleLink accessRequestStatus(
    OpaqueChronicleLinkAccessRequestStatusEnum? accessRequestStatus,
  ) => call(accessRequestStatus: accessRequestStatus);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OpaqueChronicleLink(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OpaqueChronicleLink(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  OpaqueChronicleLink call({
    Object? linkId = const $CopyWithPlaceholder(),
    Object? disclosure = const $CopyWithPlaceholder(),
    Object? chronicleId = const $CopyWithPlaceholder(),
    Object? strength = const $CopyWithPlaceholder(),
    Object? dimensions = const $CopyWithPlaceholder(),
    Object? timeOverlap = const $CopyWithPlaceholder(),
    Object? remote = const $CopyWithPlaceholder(),
    Object? ownerHandle = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? accessRequestStatus = const $CopyWithPlaceholder(),
  }) {
    return OpaqueChronicleLink(
      linkId: linkId == const $CopyWithPlaceholder() || linkId == null
          ? _value.linkId
          // ignore: cast_nullable_to_non_nullable
          : linkId as String,
      disclosure:
          disclosure == const $CopyWithPlaceholder() || disclosure == null
          ? _value.disclosure
          // ignore: cast_nullable_to_non_nullable
          : disclosure as OpaqueChronicleLinkDisclosureEnum,
      chronicleId: chronicleId == const $CopyWithPlaceholder()
          ? _value.chronicleId
          // ignore: cast_nullable_to_non_nullable
          : chronicleId as String?,
      strength: strength == const $CopyWithPlaceholder() || strength == null
          ? _value.strength
          // ignore: cast_nullable_to_non_nullable
          : strength as OpaqueChronicleLinkStrengthEnum,
      dimensions:
          dimensions == const $CopyWithPlaceholder() || dimensions == null
          ? _value.dimensions
          // ignore: cast_nullable_to_non_nullable
          : dimensions as List<OpaqueChronicleLinkDimensionsEnum>,
      timeOverlap:
          timeOverlap == const $CopyWithPlaceholder() || timeOverlap == null
          ? _value.timeOverlap
          // ignore: cast_nullable_to_non_nullable
          : timeOverlap as OpaqueChronicleLinkTimeOverlapEnum,
      remote: remote == const $CopyWithPlaceholder()
          ? _value.remote
          // ignore: cast_nullable_to_non_nullable
          : remote as bool?,
      ownerHandle: ownerHandle == const $CopyWithPlaceholder()
          ? _value.ownerHandle
          // ignore: cast_nullable_to_non_nullable
          : ownerHandle as String?,
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      accessRequestStatus: accessRequestStatus == const $CopyWithPlaceholder()
          ? _value.accessRequestStatus
          // ignore: cast_nullable_to_non_nullable
          : accessRequestStatus as OpaqueChronicleLinkAccessRequestStatusEnum?,
    );
  }
}

extension $OpaqueChronicleLinkCopyWith on OpaqueChronicleLink {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfOpaqueChronicleLink.copyWith(...)` or `instanceOfOpaqueChronicleLink.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OpaqueChronicleLinkCWProxy get copyWith =>
      _$OpaqueChronicleLinkCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OpaqueChronicleLink _$OpaqueChronicleLinkFromJson(Map<String, dynamic> json) =>
    $checkedCreate('OpaqueChronicleLink', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'linkId',
          'disclosure',
          'strength',
          'dimensions',
          'timeOverlap',
        ],
      );
      final val = OpaqueChronicleLink(
        linkId: $checkedConvert('linkId', (v) => v as String),
        disclosure: $checkedConvert(
          'disclosure',
          (v) => $enumDecode(_$OpaqueChronicleLinkDisclosureEnumEnumMap, v),
        ),
        chronicleId: $checkedConvert('chronicleId', (v) => v as String?),
        strength: $checkedConvert(
          'strength',
          (v) => $enumDecode(_$OpaqueChronicleLinkStrengthEnumEnumMap, v),
        ),
        dimensions: $checkedConvert(
          'dimensions',
          (v) => (v as List<dynamic>)
              .map(
                (e) =>
                    $enumDecode(_$OpaqueChronicleLinkDimensionsEnumEnumMap, e),
              )
              .toList(),
        ),
        timeOverlap: $checkedConvert(
          'timeOverlap',
          (v) => $enumDecode(_$OpaqueChronicleLinkTimeOverlapEnumEnumMap, v),
        ),
        remote: $checkedConvert('remote', (v) => v as bool?),
        ownerHandle: $checkedConvert('ownerHandle', (v) => v as String?),
        title: $checkedConvert('title', (v) => v as String?),
        accessRequestStatus: $checkedConvert(
          'accessRequestStatus',
          (v) => $enumDecodeNullable(
            _$OpaqueChronicleLinkAccessRequestStatusEnumEnumMap,
            v,
          ),
        ),
      );
      return val;
    });

Map<String, dynamic> _$OpaqueChronicleLinkToJson(
  OpaqueChronicleLink instance,
) => <String, dynamic>{
  'linkId': instance.linkId,
  'disclosure':
      _$OpaqueChronicleLinkDisclosureEnumEnumMap[instance.disclosure]!,
  'chronicleId': ?instance.chronicleId,
  'strength': _$OpaqueChronicleLinkStrengthEnumEnumMap[instance.strength]!,
  'dimensions': instance.dimensions
      .map((e) => _$OpaqueChronicleLinkDimensionsEnumEnumMap[e]!)
      .toList(),
  'timeOverlap':
      _$OpaqueChronicleLinkTimeOverlapEnumEnumMap[instance.timeOverlap]!,
  'remote': ?instance.remote,
  'ownerHandle': ?instance.ownerHandle,
  'title': ?instance.title,
  'accessRequestStatus':
      ?_$OpaqueChronicleLinkAccessRequestStatusEnumEnumMap[instance
          .accessRequestStatus],
};

const _$OpaqueChronicleLinkDisclosureEnumEnumMap = {
  OpaqueChronicleLinkDisclosureEnum.opaque: 'opaque',
};

const _$OpaqueChronicleLinkStrengthEnumEnumMap = {
  OpaqueChronicleLinkStrengthEnum.low: 'low',
  OpaqueChronicleLinkStrengthEnum.medium: 'medium',
  OpaqueChronicleLinkStrengthEnum.high: 'high',
};

const _$OpaqueChronicleLinkDimensionsEnumEnumMap = {
  OpaqueChronicleLinkDimensionsEnum.evidence: 'evidence',
  OpaqueChronicleLinkDimensionsEnum.entities: 'entities',
  OpaqueChronicleLinkDimensionsEnum.areaTime: 'area-time',
  OpaqueChronicleLinkDimensionsEnum.topics: 'topics',
};

const _$OpaqueChronicleLinkTimeOverlapEnumEnumMap = {
  OpaqueChronicleLinkTimeOverlapEnum.none: 'none',
  OpaqueChronicleLinkTimeOverlapEnum.partial: 'partial',
  OpaqueChronicleLinkTimeOverlapEnum.full: 'full',
};

const _$OpaqueChronicleLinkAccessRequestStatusEnumEnumMap = {
  OpaqueChronicleLinkAccessRequestStatusEnum.pending: 'pending',
  OpaqueChronicleLinkAccessRequestStatusEnum.granted: 'granted',
  OpaqueChronicleLinkAccessRequestStatusEnum.declined: 'declined',
};
