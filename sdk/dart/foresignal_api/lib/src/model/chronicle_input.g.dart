// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleInputCWProxy {
  ChronicleInput name(String name);

  ChronicleInput description(String? description);

  ChronicleInput visibility(Visibility? visibility);

  ChronicleInput tlp(Tlp? tlp);

  ChronicleInput area(ChronicleArea? area);

  ChronicleInput retention(String? retention);

  ChronicleInput sizeCapBytes(int? sizeCapBytes);

  ChronicleInput situationIds(List<String>? situationIds);

  ChronicleInput hypothesisSetIds(List<String>? hypothesisSetIds);

  ChronicleInput investigationIds(List<String>? investigationIds);

  ChronicleInput topicIds(List<String>? topicIds);

  ChronicleInput layers(List<ChronicleLayerInput>? layers);

  ChronicleInput fromWorkspaceId(String? fromWorkspaceId);

  ChronicleInput activate(bool? activate);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleInput(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleInput call({
    String name,
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
    List<ChronicleLayerInput>? layers,
    String? fromWorkspaceId,
    bool? activate,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleInput.copyWith(...)` or call `instanceOfChronicleInput.copyWith.fieldName(value)` for a single field.
class _$ChronicleInputCWProxyImpl implements _$ChronicleInputCWProxy {
  const _$ChronicleInputCWProxyImpl(this._value);

  final ChronicleInput _value;

  @override
  ChronicleInput name(String name) => call(name: name);

  @override
  ChronicleInput description(String? description) =>
      call(description: description);

  @override
  ChronicleInput visibility(Visibility? visibility) =>
      call(visibility: visibility);

  @override
  ChronicleInput tlp(Tlp? tlp) => call(tlp: tlp);

  @override
  ChronicleInput area(ChronicleArea? area) => call(area: area);

  @override
  ChronicleInput retention(String? retention) => call(retention: retention);

  @override
  ChronicleInput sizeCapBytes(int? sizeCapBytes) =>
      call(sizeCapBytes: sizeCapBytes);

  @override
  ChronicleInput situationIds(List<String>? situationIds) =>
      call(situationIds: situationIds);

  @override
  ChronicleInput hypothesisSetIds(List<String>? hypothesisSetIds) =>
      call(hypothesisSetIds: hypothesisSetIds);

  @override
  ChronicleInput investigationIds(List<String>? investigationIds) =>
      call(investigationIds: investigationIds);

  @override
  ChronicleInput topicIds(List<String>? topicIds) => call(topicIds: topicIds);

  @override
  ChronicleInput layers(List<ChronicleLayerInput>? layers) =>
      call(layers: layers);

  @override
  ChronicleInput fromWorkspaceId(String? fromWorkspaceId) =>
      call(fromWorkspaceId: fromWorkspaceId);

  @override
  ChronicleInput activate(bool? activate) => call(activate: activate);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleInput call({
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
    Object? layers = const $CopyWithPlaceholder(),
    Object? fromWorkspaceId = const $CopyWithPlaceholder(),
    Object? activate = const $CopyWithPlaceholder(),
  }) {
    return ChronicleInput(
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
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
      layers: layers == const $CopyWithPlaceholder()
          ? _value.layers
          // ignore: cast_nullable_to_non_nullable
          : layers as List<ChronicleLayerInput>?,
      fromWorkspaceId: fromWorkspaceId == const $CopyWithPlaceholder()
          ? _value.fromWorkspaceId
          // ignore: cast_nullable_to_non_nullable
          : fromWorkspaceId as String?,
      activate: activate == const $CopyWithPlaceholder()
          ? _value.activate
          // ignore: cast_nullable_to_non_nullable
          : activate as bool?,
    );
  }
}

extension $ChronicleInputCopyWith on ChronicleInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleInput.copyWith(...)` or `instanceOfChronicleInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleInputCWProxy get copyWith => _$ChronicleInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleInput _$ChronicleInputFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChronicleInput', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['name']);
  final val = ChronicleInput(
    name: $checkedConvert('name', (v) => v as String),
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
    layers: $checkedConvert(
      'layers',
      (v) => (v as List<dynamic>?)
          ?.map((e) => ChronicleLayerInput.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    fromWorkspaceId: $checkedConvert('fromWorkspaceId', (v) => v as String?),
    activate: $checkedConvert('activate', (v) => v as bool? ?? false),
  );
  return val;
});

Map<String, dynamic> _$ChronicleInputToJson(ChronicleInput instance) =>
    <String, dynamic>{
      'name': instance.name,
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
      'layers': ?instance.layers?.map((e) => e.toJson()).toList(),
      'fromWorkspaceId': ?instance.fromWorkspaceId,
      'activate': ?instance.activate,
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
