// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'object_envelope.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ObjectEnvelopeCWProxy {
  ObjectEnvelope ref(String ref);

  ObjectEnvelope type(String type);

  ObjectEnvelope title(String title);

  ObjectEnvelope summary(String? summary);

  ObjectEnvelope textOrigin(ObjectEnvelopeTextOriginEnum? textOrigin);

  ObjectEnvelope status(String? status);

  ObjectEnvelope severity(String? severity);

  ObjectEnvelope time(ObjectEnvelopeTime? time);

  ObjectEnvelope geometry(Geometry? geometry);

  ObjectEnvelope bbox(List<num>? bbox);

  ObjectEnvelope private(bool? private);

  ObjectEnvelope tlp(Tlp? tlp);

  ObjectEnvelope tags(List<String>? tags);

  ObjectEnvelope provenance(Provenance? provenance);

  ObjectEnvelope links(ObjectEnvelopeLinks? links);

  ObjectEnvelope actions(List<ObjectAction>? actions);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ObjectEnvelope(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ObjectEnvelope(...).copyWith(id: 12, name: "My name")
  /// ```
  ObjectEnvelope call({
    String ref,
    String type,
    String title,
    String? summary,
    ObjectEnvelopeTextOriginEnum? textOrigin,
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
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfObjectEnvelope.copyWith(...)` or call `instanceOfObjectEnvelope.copyWith.fieldName(value)` for a single field.
class _$ObjectEnvelopeCWProxyImpl implements _$ObjectEnvelopeCWProxy {
  const _$ObjectEnvelopeCWProxyImpl(this._value);

  final ObjectEnvelope _value;

  @override
  ObjectEnvelope ref(String ref) => call(ref: ref);

  @override
  ObjectEnvelope type(String type) => call(type: type);

  @override
  ObjectEnvelope title(String title) => call(title: title);

  @override
  ObjectEnvelope summary(String? summary) => call(summary: summary);

  @override
  ObjectEnvelope textOrigin(ObjectEnvelopeTextOriginEnum? textOrigin) =>
      call(textOrigin: textOrigin);

  @override
  ObjectEnvelope status(String? status) => call(status: status);

  @override
  ObjectEnvelope severity(String? severity) => call(severity: severity);

  @override
  ObjectEnvelope time(ObjectEnvelopeTime? time) => call(time: time);

  @override
  ObjectEnvelope geometry(Geometry? geometry) => call(geometry: geometry);

  @override
  ObjectEnvelope bbox(List<num>? bbox) => call(bbox: bbox);

  @override
  ObjectEnvelope private(bool? private) => call(private: private);

  @override
  ObjectEnvelope tlp(Tlp? tlp) => call(tlp: tlp);

  @override
  ObjectEnvelope tags(List<String>? tags) => call(tags: tags);

  @override
  ObjectEnvelope provenance(Provenance? provenance) =>
      call(provenance: provenance);

  @override
  ObjectEnvelope links(ObjectEnvelopeLinks? links) => call(links: links);

  @override
  ObjectEnvelope actions(List<ObjectAction>? actions) => call(actions: actions);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ObjectEnvelope(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ObjectEnvelope(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ObjectEnvelope call({
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
  }) {
    return ObjectEnvelope(
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
          : textOrigin as ObjectEnvelopeTextOriginEnum?,
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
    );
  }
}

extension $ObjectEnvelopeCopyWith on ObjectEnvelope {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfObjectEnvelope.copyWith(...)` or `instanceOfObjectEnvelope.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ObjectEnvelopeCWProxy get copyWith => _$ObjectEnvelopeCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ObjectEnvelope _$ObjectEnvelopeFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ObjectEnvelope', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['ref', 'type', 'title']);
      final val = ObjectEnvelope(
        ref: $checkedConvert('ref', (v) => v as String),
        type: $checkedConvert('type', (v) => v as String),
        title: $checkedConvert('title', (v) => v as String),
        summary: $checkedConvert('summary', (v) => v as String?),
        textOrigin: $checkedConvert(
          'textOrigin',
          (v) => $enumDecodeNullable(_$ObjectEnvelopeTextOriginEnumEnumMap, v),
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
          (v) =>
              v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
        ),
        bbox: $checkedConvert(
          'bbox',
          (v) => (v as List<dynamic>?)?.map((e) => e as num).toList(),
        ),
        private: $checkedConvert('private', (v) => v as bool?),
        tlp: $checkedConvert(
          'tlp',
          (v) => $enumDecodeNullable(_$TlpEnumMap, v),
        ),
        tags: $checkedConvert(
          'tags',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        provenance: $checkedConvert(
          'provenance',
          (v) =>
              v == null ? null : Provenance.fromJson(v as Map<String, dynamic>),
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
      );
      return val;
    });

Map<String, dynamic> _$ObjectEnvelopeToJson(ObjectEnvelope instance) =>
    <String, dynamic>{
      'ref': instance.ref,
      'type': instance.type,
      'title': instance.title,
      'summary': ?instance.summary,
      'textOrigin': ?_$ObjectEnvelopeTextOriginEnumEnumMap[instance.textOrigin],
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
    };

const _$ObjectEnvelopeTextOriginEnumEnumMap = {
  ObjectEnvelopeTextOriginEnum.deterministic: 'deterministic',
  ObjectEnvelopeTextOriginEnum.llm: 'llm',
};

const _$TlpEnumMap = {
  Tlp.CLEAR: 'CLEAR',
  Tlp.GREEN: 'GREEN',
  Tlp.AMBER: 'AMBER',
  Tlp.aMBERPlusSTRICT: 'AMBER+STRICT',
  Tlp.RED: 'RED',
};
