// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'sensor.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SensorCWProxy {
  Sensor ref(String ref);

  Sensor type(String type);

  Sensor title(String title);

  Sensor summary(String? summary);

  Sensor textOrigin(SensorTextOriginEnum? textOrigin);

  Sensor status(String? status);

  Sensor severity(String? severity);

  Sensor time(ObjectEnvelopeTime? time);

  Sensor geometry(Geometry? geometry);

  Sensor bbox(List<num>? bbox);

  Sensor private(bool? private);

  Sensor tlp(Tlp? tlp);

  Sensor tags(List<String>? tags);

  Sensor provenance(Provenance? provenance);

  Sensor links(ObjectEnvelopeLinks? links);

  Sensor actions(List<ObjectAction>? actions);

  Sensor sensorKind(SensorSensorKindEnum sensorKind);

  Sensor pDetect(num? pDetect);

  Sensor method(String? method);

  Sensor families(List<String>? families);

  Sensor rangeKm(num? rangeKm);

  Sensor passes(List<SensorAllOfPasses>? passes);

  Sensor capabilities(SensorAllOfCapabilities? capabilities);

  Sensor footprint(Geometry? footprint);

  Sensor acquiredAt(DateTime? acquiredAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Sensor(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Sensor(...).copyWith(id: 12, name: "My name")
  /// ```
  Sensor call({
    String ref,
    String type,
    String title,
    String? summary,
    SensorTextOriginEnum? textOrigin,
    String? status,
    String? severity,
    ObjectEnvelopeTime? time,
    Geometry? geometry,
    List<num>? bbox,
    bool? private,
    Tlp? tlp,
    List<String>? tags,
    Provenance? provenance,
    ObjectEnvelopeLinks? links,
    List<ObjectAction>? actions,
    SensorSensorKindEnum sensorKind,
    num? pDetect,
    String? method,
    List<String>? families,
    num? rangeKm,
    List<SensorAllOfPasses>? passes,
    SensorAllOfCapabilities? capabilities,
    Geometry? footprint,
    DateTime? acquiredAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSensor.copyWith(...)` or call `instanceOfSensor.copyWith.fieldName(value)` for a single field.
class _$SensorCWProxyImpl implements _$SensorCWProxy {
  const _$SensorCWProxyImpl(this._value);

  final Sensor _value;

  @override
  Sensor ref(String ref) => call(ref: ref);

  @override
  Sensor type(String type) => call(type: type);

  @override
  Sensor title(String title) => call(title: title);

  @override
  Sensor summary(String? summary) => call(summary: summary);

  @override
  Sensor textOrigin(SensorTextOriginEnum? textOrigin) =>
      call(textOrigin: textOrigin);

  @override
  Sensor status(String? status) => call(status: status);

  @override
  Sensor severity(String? severity) => call(severity: severity);

  @override
  Sensor time(ObjectEnvelopeTime? time) => call(time: time);

  @override
  Sensor geometry(Geometry? geometry) => call(geometry: geometry);

  @override
  Sensor bbox(List<num>? bbox) => call(bbox: bbox);

  @override
  Sensor private(bool? private) => call(private: private);

  @override
  Sensor tlp(Tlp? tlp) => call(tlp: tlp);

  @override
  Sensor tags(List<String>? tags) => call(tags: tags);

  @override
  Sensor provenance(Provenance? provenance) => call(provenance: provenance);

  @override
  Sensor links(ObjectEnvelopeLinks? links) => call(links: links);

  @override
  Sensor actions(List<ObjectAction>? actions) => call(actions: actions);

  @override
  Sensor sensorKind(SensorSensorKindEnum sensorKind) =>
      call(sensorKind: sensorKind);

  @override
  Sensor pDetect(num? pDetect) => call(pDetect: pDetect);

  @override
  Sensor method(String? method) => call(method: method);

  @override
  Sensor families(List<String>? families) => call(families: families);

  @override
  Sensor rangeKm(num? rangeKm) => call(rangeKm: rangeKm);

  @override
  Sensor passes(List<SensorAllOfPasses>? passes) => call(passes: passes);

  @override
  Sensor capabilities(SensorAllOfCapabilities? capabilities) =>
      call(capabilities: capabilities);

  @override
  Sensor footprint(Geometry? footprint) => call(footprint: footprint);

  @override
  Sensor acquiredAt(DateTime? acquiredAt) => call(acquiredAt: acquiredAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Sensor(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Sensor(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Sensor call({
    Object? ref = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? summary = const $CopyWithPlaceholder(),
    Object? textOrigin = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? severity = const $CopyWithPlaceholder(),
    Object? time = const $CopyWithPlaceholder(),
    Object? geometry = const $CopyWithPlaceholder(),
    Object? bbox = const $CopyWithPlaceholder(),
    Object? private = const $CopyWithPlaceholder(),
    Object? tlp = const $CopyWithPlaceholder(),
    Object? tags = const $CopyWithPlaceholder(),
    Object? provenance = const $CopyWithPlaceholder(),
    Object? links = const $CopyWithPlaceholder(),
    Object? actions = const $CopyWithPlaceholder(),
    Object? sensorKind = const $CopyWithPlaceholder(),
    Object? pDetect = const $CopyWithPlaceholder(),
    Object? method = const $CopyWithPlaceholder(),
    Object? families = const $CopyWithPlaceholder(),
    Object? rangeKm = const $CopyWithPlaceholder(),
    Object? passes = const $CopyWithPlaceholder(),
    Object? capabilities = const $CopyWithPlaceholder(),
    Object? footprint = const $CopyWithPlaceholder(),
    Object? acquiredAt = const $CopyWithPlaceholder(),
  }) {
    return Sensor(
      ref: ref == const $CopyWithPlaceholder() || ref == null
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as String,
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      summary: summary == const $CopyWithPlaceholder()
          ? _value.summary
          // ignore: cast_nullable_to_non_nullable
          : summary as String?,
      textOrigin: textOrigin == const $CopyWithPlaceholder()
          ? _value.textOrigin
          // ignore: cast_nullable_to_non_nullable
          : textOrigin as SensorTextOriginEnum?,
      status: status == const $CopyWithPlaceholder()
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as String?,
      severity: severity == const $CopyWithPlaceholder()
          ? _value.severity
          // ignore: cast_nullable_to_non_nullable
          : severity as String?,
      time: time == const $CopyWithPlaceholder()
          ? _value.time
          // ignore: cast_nullable_to_non_nullable
          : time as ObjectEnvelopeTime?,
      geometry: geometry == const $CopyWithPlaceholder()
          ? _value.geometry
          // ignore: cast_nullable_to_non_nullable
          : geometry as Geometry?,
      bbox: bbox == const $CopyWithPlaceholder()
          ? _value.bbox
          // ignore: cast_nullable_to_non_nullable
          : bbox as List<num>?,
      private: private == const $CopyWithPlaceholder()
          ? _value.private
          // ignore: cast_nullable_to_non_nullable
          : private as bool?,
      tlp: tlp == const $CopyWithPlaceholder()
          ? _value.tlp
          // ignore: cast_nullable_to_non_nullable
          : tlp as Tlp?,
      tags: tags == const $CopyWithPlaceholder()
          ? _value.tags
          // ignore: cast_nullable_to_non_nullable
          : tags as List<String>?,
      provenance: provenance == const $CopyWithPlaceholder()
          ? _value.provenance
          // ignore: cast_nullable_to_non_nullable
          : provenance as Provenance?,
      links: links == const $CopyWithPlaceholder()
          ? _value.links
          // ignore: cast_nullable_to_non_nullable
          : links as ObjectEnvelopeLinks?,
      actions: actions == const $CopyWithPlaceholder()
          ? _value.actions
          // ignore: cast_nullable_to_non_nullable
          : actions as List<ObjectAction>?,
      sensorKind:
          sensorKind == const $CopyWithPlaceholder() || sensorKind == null
          ? _value.sensorKind
          // ignore: cast_nullable_to_non_nullable
          : sensorKind as SensorSensorKindEnum,
      pDetect: pDetect == const $CopyWithPlaceholder()
          ? _value.pDetect
          // ignore: cast_nullable_to_non_nullable
          : pDetect as num?,
      method: method == const $CopyWithPlaceholder()
          ? _value.method
          // ignore: cast_nullable_to_non_nullable
          : method as String?,
      families: families == const $CopyWithPlaceholder()
          ? _value.families
          // ignore: cast_nullable_to_non_nullable
          : families as List<String>?,
      rangeKm: rangeKm == const $CopyWithPlaceholder()
          ? _value.rangeKm
          // ignore: cast_nullable_to_non_nullable
          : rangeKm as num?,
      passes: passes == const $CopyWithPlaceholder()
          ? _value.passes
          // ignore: cast_nullable_to_non_nullable
          : passes as List<SensorAllOfPasses>?,
      capabilities: capabilities == const $CopyWithPlaceholder()
          ? _value.capabilities
          // ignore: cast_nullable_to_non_nullable
          : capabilities as SensorAllOfCapabilities?,
      footprint: footprint == const $CopyWithPlaceholder()
          ? _value.footprint
          // ignore: cast_nullable_to_non_nullable
          : footprint as Geometry?,
      acquiredAt: acquiredAt == const $CopyWithPlaceholder()
          ? _value.acquiredAt
          // ignore: cast_nullable_to_non_nullable
          : acquiredAt as DateTime?,
    );
  }
}

extension $SensorCopyWith on Sensor {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSensor.copyWith(...)` or `instanceOfSensor.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SensorCWProxy get copyWith => _$SensorCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Sensor _$SensorFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('Sensor', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['ref', 'type', 'title', 'sensorKind']);
  final val = Sensor(
    ref: $checkedConvert('ref', (v) => v as String),
    type: $checkedConvert('type', (v) => v as String),
    title: $checkedConvert('title', (v) => v as String),
    summary: $checkedConvert('summary', (v) => v as String?),
    textOrigin: $checkedConvert(
      'textOrigin',
      (v) => $enumDecodeNullable(_$SensorTextOriginEnumEnumMap, v),
    ),
    status: $checkedConvert('status', (v) => v as String?),
    severity: $checkedConvert('severity', (v) => v as String?),
    time: $checkedConvert(
      'time',
      (v) => v == null
          ? null
          : ObjectEnvelopeTime.fromJson(v as Map<String, dynamic>),
    ),
    geometry: $checkedConvert(
      'geometry',
      (v) => v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
    ),
    bbox: $checkedConvert(
      'bbox',
      (v) => (v as List<dynamic>?)?.map((e) => e as num).toList(),
    ),
    private: $checkedConvert('private', (v) => v as bool?),
    tlp: $checkedConvert('tlp', (v) => $enumDecodeNullable(_$TlpEnumMap, v)),
    tags: $checkedConvert(
      'tags',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    provenance: $checkedConvert(
      'provenance',
      (v) => v == null ? null : Provenance.fromJson(v as Map<String, dynamic>),
    ),
    links: $checkedConvert(
      'links',
      (v) => v == null
          ? null
          : ObjectEnvelopeLinks.fromJson(v as Map<String, dynamic>),
    ),
    actions: $checkedConvert(
      'actions',
      (v) => (v as List<dynamic>?)
          ?.map((e) => ObjectAction.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    sensorKind: $checkedConvert(
      'sensorKind',
      (v) => $enumDecode(_$SensorSensorKindEnumEnumMap, v),
    ),
    pDetect: $checkedConvert('pDetect', (v) => v as num?),
    method: $checkedConvert('method', (v) => v as String?),
    families: $checkedConvert(
      'families',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    rangeKm: $checkedConvert('rangeKm', (v) => v as num?),
    passes: $checkedConvert(
      'passes',
      (v) => (v as List<dynamic>?)
          ?.map((e) => SensorAllOfPasses.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    capabilities: $checkedConvert(
      'capabilities',
      (v) => v == null
          ? null
          : SensorAllOfCapabilities.fromJson(v as Map<String, dynamic>),
    ),
    footprint: $checkedConvert(
      'footprint',
      (v) => v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
    ),
    acquiredAt: $checkedConvert(
      'acquiredAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$SensorToJson(Sensor instance) => <String, dynamic>{
  'ref': instance.ref,
  'type': instance.type,
  'title': instance.title,
  'summary': ?instance.summary,
  'textOrigin': ?_$SensorTextOriginEnumEnumMap[instance.textOrigin],
  'status': ?instance.status,
  'severity': ?instance.severity,
  'time': ?instance.time?.toJson(),
  'geometry': ?instance.geometry?.toJson(),
  'bbox': ?instance.bbox,
  'private': ?instance.private,
  'tlp': ?_$TlpEnumMap[instance.tlp],
  'tags': ?instance.tags,
  'provenance': ?instance.provenance?.toJson(),
  'links': ?instance.links?.toJson(),
  'actions': ?instance.actions?.map((e) => e.toJson()).toList(),
  'sensorKind': _$SensorSensorKindEnumEnumMap[instance.sensorKind]!,
  'pDetect': ?instance.pDetect,
  'method': ?instance.method,
  'families': ?instance.families,
  'rangeKm': ?instance.rangeKm,
  'passes': ?instance.passes?.map((e) => e.toJson()).toList(),
  'capabilities': ?instance.capabilities?.toJson(),
  'footprint': ?instance.footprint?.toJson(),
  'acquiredAt': ?instance.acquiredAt?.toIso8601String(),
};

const _$SensorTextOriginEnumEnumMap = {
  SensorTextOriginEnum.deterministic: 'deterministic',
  SensorTextOriginEnum.llm: 'llm',
};

const _$TlpEnumMap = {
  Tlp.CLEAR: 'CLEAR',
  Tlp.GREEN: 'GREEN',
  Tlp.AMBER: 'AMBER',
  Tlp.aMBERPlusSTRICT: 'AMBER+STRICT',
  Tlp.RED: 'RED',
};

const _$SensorSensorKindEnumEnumMap = {
  SensorSensorKindEnum.source_: 'source',
  SensorSensorKindEnum.receiver: 'receiver',
  SensorSensorKindEnum.station: 'station',
  SensorSensorKindEnum.satellite: 'satellite',
  SensorSensorKindEnum.scene: 'scene',
  SensorSensorKindEnum.rasterProduct: 'raster-product',
};
