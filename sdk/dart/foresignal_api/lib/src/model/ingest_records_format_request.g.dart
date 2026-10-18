// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ingest_records_format_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IngestRecordsFormatRequestCWProxy {
  IngestRecordsFormatRequest type(String type);

  IngestRecordsFormatRequest features(List<Feature> features);

  IngestRecordsFormatRequest nextCursor(String? nextCursor);

  IngestRecordsFormatRequest attributions(List<Attribution>? attributions);

  IngestRecordsFormatRequest watermarkSeq(int? watermarkSeq);

  IngestRecordsFormatRequest asOfSeq(int? asOfSeq);

  IngestRecordsFormatRequest asOf(DateTime? asOf);

  IngestRecordsFormatRequest approximate(bool? approximate);

  IngestRecordsFormatRequest horizon(DateTime? horizon);

  IngestRecordsFormatRequest licenceFiltered(int? licenceFiltered);

  IngestRecordsFormatRequest records(List<NormalizedRecordPreview> records);

  IngestRecordsFormatRequest version(
    IngestRecordsFormatRequestVersionEnum version,
  );

  IngestRecordsFormatRequest groupKey(String? groupKey);

  IngestRecordsFormatRequest truncatedAlerts(int? truncatedAlerts);

  IngestRecordsFormatRequest status(
    IngestRecordsFormatRequestStatusEnum? status,
  );

  IngestRecordsFormatRequest receiver(String? receiver);

  IngestRecordsFormatRequest groupLabels(Map<String, String>? groupLabels);

  IngestRecordsFormatRequest commonLabels(Map<String, String>? commonLabels);

  IngestRecordsFormatRequest commonAnnotations(
    Map<String, String>? commonAnnotations,
  );

  IngestRecordsFormatRequest externalURL(String? externalURL);

  IngestRecordsFormatRequest alerts(
    List<AlertmanagerWebhookAlertsInner> alerts,
  );

  IngestRecordsFormatRequest specversion(
    IngestRecordsFormatRequestSpecversionEnum specversion,
  );

  IngestRecordsFormatRequest id(String id);

  IngestRecordsFormatRequest source_(String source_);

  IngestRecordsFormatRequest subject(String? subject);

  IngestRecordsFormatRequest time(String? time);

  IngestRecordsFormatRequest datacontenttype(String? datacontenttype);

  IngestRecordsFormatRequest dataschema(String? dataschema);

  IngestRecordsFormatRequest data(Object? data);

  IngestRecordsFormatRequest typeUnderscore(
    IngestRecordsFormatRequestTypeUnderscoreEnum typeUnderscore,
  );

  IngestRecordsFormatRequest lat(num lat);

  IngestRecordsFormatRequest lon(num lon);

  IngestRecordsFormatRequest tst(int? tst);

  IngestRecordsFormatRequest acc(num? acc);

  IngestRecordsFormatRequest alt(num? alt);

  IngestRecordsFormatRequest vel(num? vel);

  IngestRecordsFormatRequest cog(num? cog);

  IngestRecordsFormatRequest batt(num? batt);

  IngestRecordsFormatRequest bs(int? bs);

  IngestRecordsFormatRequest conn(String? conn);

  IngestRecordsFormatRequest tid(String? tid);

  IngestRecordsFormatRequest event(String? event);

  IngestRecordsFormatRequest desc(String? desc);

  IngestRecordsFormatRequest wtst(int? wtst);

  IngestRecordsFormatRequest locations(
    List<OverlandBatchLocationsInner> locations,
  );

  IngestRecordsFormatRequest trip(Object? trip);

  IngestRecordsFormatRequest current(Object? current);

  IngestRecordsFormatRequest deviceId(String deviceId);

  IngestRecordsFormatRequest location(OsmAndLocationLocation location);

  IngestRecordsFormatRequest spd(num? spd);

  IngestRecordsFormatRequest dir(num? dir);

  IngestRecordsFormatRequest dev(String? dev);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IngestRecordsFormatRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IngestRecordsFormatRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  IngestRecordsFormatRequest call({
    String type,
    List<Feature> features,
    String? nextCursor,
    List<Attribution>? attributions,
    int? watermarkSeq,
    int? asOfSeq,
    DateTime? asOf,
    bool? approximate,
    DateTime? horizon,
    int? licenceFiltered,
    List<NormalizedRecordPreview> records,
    IngestRecordsFormatRequestVersionEnum version,
    String? groupKey,
    int? truncatedAlerts,
    IngestRecordsFormatRequestStatusEnum? status,
    String? receiver,
    Map<String, String>? groupLabels,
    Map<String, String>? commonLabels,
    Map<String, String>? commonAnnotations,
    String? externalURL,
    List<AlertmanagerWebhookAlertsInner> alerts,
    IngestRecordsFormatRequestSpecversionEnum specversion,
    String id,
    String source_,
    String? subject,
    String? time,
    String? datacontenttype,
    String? dataschema,
    Object? data,
    IngestRecordsFormatRequestTypeUnderscoreEnum typeUnderscore,
    num lat,
    num lon,
    int? tst,
    num? acc,
    num? alt,
    num? vel,
    num? cog,
    num? batt,
    int? bs,
    String? conn,
    String? tid,
    String? event,
    String? desc,
    int? wtst,
    List<OverlandBatchLocationsInner> locations,
    Object? trip,
    Object? current,
    String deviceId,
    OsmAndLocationLocation location,
    num? spd,
    num? dir,
    String? dev,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIngestRecordsFormatRequest.copyWith(...)` or call `instanceOfIngestRecordsFormatRequest.copyWith.fieldName(value)` for a single field.
class _$IngestRecordsFormatRequestCWProxyImpl
    implements _$IngestRecordsFormatRequestCWProxy {
  const _$IngestRecordsFormatRequestCWProxyImpl(this._value);

  final IngestRecordsFormatRequest _value;

  @override
  IngestRecordsFormatRequest type(String type) => call(type: type);

  @override
  IngestRecordsFormatRequest features(List<Feature> features) =>
      call(features: features);

  @override
  IngestRecordsFormatRequest nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  @override
  IngestRecordsFormatRequest attributions(List<Attribution>? attributions) =>
      call(attributions: attributions);

  @override
  IngestRecordsFormatRequest watermarkSeq(int? watermarkSeq) =>
      call(watermarkSeq: watermarkSeq);

  @override
  IngestRecordsFormatRequest asOfSeq(int? asOfSeq) => call(asOfSeq: asOfSeq);

  @override
  IngestRecordsFormatRequest asOf(DateTime? asOf) => call(asOf: asOf);

  @override
  IngestRecordsFormatRequest approximate(bool? approximate) =>
      call(approximate: approximate);

  @override
  IngestRecordsFormatRequest horizon(DateTime? horizon) =>
      call(horizon: horizon);

  @override
  IngestRecordsFormatRequest licenceFiltered(int? licenceFiltered) =>
      call(licenceFiltered: licenceFiltered);

  @override
  IngestRecordsFormatRequest records(List<NormalizedRecordPreview> records) =>
      call(records: records);

  @override
  IngestRecordsFormatRequest version(
    IngestRecordsFormatRequestVersionEnum version,
  ) => call(version: version);

  @override
  IngestRecordsFormatRequest groupKey(String? groupKey) =>
      call(groupKey: groupKey);

  @override
  IngestRecordsFormatRequest truncatedAlerts(int? truncatedAlerts) =>
      call(truncatedAlerts: truncatedAlerts);

  @override
  IngestRecordsFormatRequest status(
    IngestRecordsFormatRequestStatusEnum? status,
  ) => call(status: status);

  @override
  IngestRecordsFormatRequest receiver(String? receiver) =>
      call(receiver: receiver);

  @override
  IngestRecordsFormatRequest groupLabels(Map<String, String>? groupLabels) =>
      call(groupLabels: groupLabels);

  @override
  IngestRecordsFormatRequest commonLabels(Map<String, String>? commonLabels) =>
      call(commonLabels: commonLabels);

  @override
  IngestRecordsFormatRequest commonAnnotations(
    Map<String, String>? commonAnnotations,
  ) => call(commonAnnotations: commonAnnotations);

  @override
  IngestRecordsFormatRequest externalURL(String? externalURL) =>
      call(externalURL: externalURL);

  @override
  IngestRecordsFormatRequest alerts(
    List<AlertmanagerWebhookAlertsInner> alerts,
  ) => call(alerts: alerts);

  @override
  IngestRecordsFormatRequest specversion(
    IngestRecordsFormatRequestSpecversionEnum specversion,
  ) => call(specversion: specversion);

  @override
  IngestRecordsFormatRequest id(String id) => call(id: id);

  @override
  IngestRecordsFormatRequest source_(String source_) => call(source_: source_);

  @override
  IngestRecordsFormatRequest subject(String? subject) => call(subject: subject);

  @override
  IngestRecordsFormatRequest time(String? time) => call(time: time);

  @override
  IngestRecordsFormatRequest datacontenttype(String? datacontenttype) =>
      call(datacontenttype: datacontenttype);

  @override
  IngestRecordsFormatRequest dataschema(String? dataschema) =>
      call(dataschema: dataschema);

  @override
  IngestRecordsFormatRequest data(Object? data) => call(data: data);

  @override
  IngestRecordsFormatRequest typeUnderscore(
    IngestRecordsFormatRequestTypeUnderscoreEnum typeUnderscore,
  ) => call(typeUnderscore: typeUnderscore);

  @override
  IngestRecordsFormatRequest lat(num lat) => call(lat: lat);

  @override
  IngestRecordsFormatRequest lon(num lon) => call(lon: lon);

  @override
  IngestRecordsFormatRequest tst(int? tst) => call(tst: tst);

  @override
  IngestRecordsFormatRequest acc(num? acc) => call(acc: acc);

  @override
  IngestRecordsFormatRequest alt(num? alt) => call(alt: alt);

  @override
  IngestRecordsFormatRequest vel(num? vel) => call(vel: vel);

  @override
  IngestRecordsFormatRequest cog(num? cog) => call(cog: cog);

  @override
  IngestRecordsFormatRequest batt(num? batt) => call(batt: batt);

  @override
  IngestRecordsFormatRequest bs(int? bs) => call(bs: bs);

  @override
  IngestRecordsFormatRequest conn(String? conn) => call(conn: conn);

  @override
  IngestRecordsFormatRequest tid(String? tid) => call(tid: tid);

  @override
  IngestRecordsFormatRequest event(String? event) => call(event: event);

  @override
  IngestRecordsFormatRequest desc(String? desc) => call(desc: desc);

  @override
  IngestRecordsFormatRequest wtst(int? wtst) => call(wtst: wtst);

  @override
  IngestRecordsFormatRequest locations(
    List<OverlandBatchLocationsInner> locations,
  ) => call(locations: locations);

  @override
  IngestRecordsFormatRequest trip(Object? trip) => call(trip: trip);

  @override
  IngestRecordsFormatRequest current(Object? current) => call(current: current);

  @override
  IngestRecordsFormatRequest deviceId(String deviceId) =>
      call(deviceId: deviceId);

  @override
  IngestRecordsFormatRequest location(OsmAndLocationLocation location) =>
      call(location: location);

  @override
  IngestRecordsFormatRequest spd(num? spd) => call(spd: spd);

  @override
  IngestRecordsFormatRequest dir(num? dir) => call(dir: dir);

  @override
  IngestRecordsFormatRequest dev(String? dev) => call(dev: dev);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IngestRecordsFormatRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IngestRecordsFormatRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IngestRecordsFormatRequest call({
    Object? type = const $CopyWithPlaceholder(),
    Object? features = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
    Object? attributions = const $CopyWithPlaceholder(),
    Object? watermarkSeq = const $CopyWithPlaceholder(),
    Object? asOfSeq = const $CopyWithPlaceholder(),
    Object? asOf = const $CopyWithPlaceholder(),
    Object? approximate = const $CopyWithPlaceholder(),
    Object? horizon = const $CopyWithPlaceholder(),
    Object? licenceFiltered = const $CopyWithPlaceholder(),
    Object? records = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
    Object? groupKey = const $CopyWithPlaceholder(),
    Object? truncatedAlerts = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? receiver = const $CopyWithPlaceholder(),
    Object? groupLabels = const $CopyWithPlaceholder(),
    Object? commonLabels = const $CopyWithPlaceholder(),
    Object? commonAnnotations = const $CopyWithPlaceholder(),
    Object? externalURL = const $CopyWithPlaceholder(),
    Object? alerts = const $CopyWithPlaceholder(),
    Object? specversion = const $CopyWithPlaceholder(),
    Object? id = const $CopyWithPlaceholder(),
    Object? source_ = const $CopyWithPlaceholder(),
    Object? subject = const $CopyWithPlaceholder(),
    Object? time = const $CopyWithPlaceholder(),
    Object? datacontenttype = const $CopyWithPlaceholder(),
    Object? dataschema = const $CopyWithPlaceholder(),
    Object? data = const $CopyWithPlaceholder(),
    Object? typeUnderscore = const $CopyWithPlaceholder(),
    Object? lat = const $CopyWithPlaceholder(),
    Object? lon = const $CopyWithPlaceholder(),
    Object? tst = const $CopyWithPlaceholder(),
    Object? acc = const $CopyWithPlaceholder(),
    Object? alt = const $CopyWithPlaceholder(),
    Object? vel = const $CopyWithPlaceholder(),
    Object? cog = const $CopyWithPlaceholder(),
    Object? batt = const $CopyWithPlaceholder(),
    Object? bs = const $CopyWithPlaceholder(),
    Object? conn = const $CopyWithPlaceholder(),
    Object? tid = const $CopyWithPlaceholder(),
    Object? event = const $CopyWithPlaceholder(),
    Object? desc = const $CopyWithPlaceholder(),
    Object? wtst = const $CopyWithPlaceholder(),
    Object? locations = const $CopyWithPlaceholder(),
    Object? trip = const $CopyWithPlaceholder(),
    Object? current = const $CopyWithPlaceholder(),
    Object? deviceId = const $CopyWithPlaceholder(),
    Object? location = const $CopyWithPlaceholder(),
    Object? spd = const $CopyWithPlaceholder(),
    Object? dir = const $CopyWithPlaceholder(),
    Object? dev = const $CopyWithPlaceholder(),
  }) {
    return IngestRecordsFormatRequest(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as String,
      features: features == const $CopyWithPlaceholder() || features == null
          ? _value.features
          // ignore: cast_nullable_to_non_nullable
          : features as List<Feature>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
      attributions: attributions == const $CopyWithPlaceholder()
          ? _value.attributions
          // ignore: cast_nullable_to_non_nullable
          : attributions as List<Attribution>?,
      watermarkSeq: watermarkSeq == const $CopyWithPlaceholder()
          ? _value.watermarkSeq
          // ignore: cast_nullable_to_non_nullable
          : watermarkSeq as int?,
      asOfSeq: asOfSeq == const $CopyWithPlaceholder()
          ? _value.asOfSeq
          // ignore: cast_nullable_to_non_nullable
          : asOfSeq as int?,
      asOf: asOf == const $CopyWithPlaceholder()
          ? _value.asOf
          // ignore: cast_nullable_to_non_nullable
          : asOf as DateTime?,
      approximate: approximate == const $CopyWithPlaceholder()
          ? _value.approximate
          // ignore: cast_nullable_to_non_nullable
          : approximate as bool?,
      horizon: horizon == const $CopyWithPlaceholder()
          ? _value.horizon
          // ignore: cast_nullable_to_non_nullable
          : horizon as DateTime?,
      licenceFiltered: licenceFiltered == const $CopyWithPlaceholder()
          ? _value.licenceFiltered
          // ignore: cast_nullable_to_non_nullable
          : licenceFiltered as int?,
      records: records == const $CopyWithPlaceholder() || records == null
          ? _value.records
          // ignore: cast_nullable_to_non_nullable
          : records as List<NormalizedRecordPreview>,
      version: version == const $CopyWithPlaceholder() || version == null
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as IngestRecordsFormatRequestVersionEnum,
      groupKey: groupKey == const $CopyWithPlaceholder()
          ? _value.groupKey
          // ignore: cast_nullable_to_non_nullable
          : groupKey as String?,
      truncatedAlerts: truncatedAlerts == const $CopyWithPlaceholder()
          ? _value.truncatedAlerts
          // ignore: cast_nullable_to_non_nullable
          : truncatedAlerts as int?,
      status: status == const $CopyWithPlaceholder()
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as IngestRecordsFormatRequestStatusEnum?,
      receiver: receiver == const $CopyWithPlaceholder()
          ? _value.receiver
          // ignore: cast_nullable_to_non_nullable
          : receiver as String?,
      groupLabels: groupLabels == const $CopyWithPlaceholder()
          ? _value.groupLabels
          // ignore: cast_nullable_to_non_nullable
          : groupLabels as Map<String, String>?,
      commonLabels: commonLabels == const $CopyWithPlaceholder()
          ? _value.commonLabels
          // ignore: cast_nullable_to_non_nullable
          : commonLabels as Map<String, String>?,
      commonAnnotations: commonAnnotations == const $CopyWithPlaceholder()
          ? _value.commonAnnotations
          // ignore: cast_nullable_to_non_nullable
          : commonAnnotations as Map<String, String>?,
      externalURL: externalURL == const $CopyWithPlaceholder()
          ? _value.externalURL
          // ignore: cast_nullable_to_non_nullable
          : externalURL as String?,
      alerts: alerts == const $CopyWithPlaceholder() || alerts == null
          ? _value.alerts
          // ignore: cast_nullable_to_non_nullable
          : alerts as List<AlertmanagerWebhookAlertsInner>,
      specversion:
          specversion == const $CopyWithPlaceholder() || specversion == null
          ? _value.specversion
          // ignore: cast_nullable_to_non_nullable
          : specversion as IngestRecordsFormatRequestSpecversionEnum,
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      source_: source_ == const $CopyWithPlaceholder() || source_ == null
          ? _value.source_
          // ignore: cast_nullable_to_non_nullable
          : source_ as String,
      subject: subject == const $CopyWithPlaceholder()
          ? _value.subject
          // ignore: cast_nullable_to_non_nullable
          : subject as String?,
      time: time == const $CopyWithPlaceholder()
          ? _value.time
          // ignore: cast_nullable_to_non_nullable
          : time as String?,
      datacontenttype: datacontenttype == const $CopyWithPlaceholder()
          ? _value.datacontenttype
          // ignore: cast_nullable_to_non_nullable
          : datacontenttype as String?,
      dataschema: dataschema == const $CopyWithPlaceholder()
          ? _value.dataschema
          // ignore: cast_nullable_to_non_nullable
          : dataschema as String?,
      data: data == const $CopyWithPlaceholder()
          ? _value.data
          // ignore: cast_nullable_to_non_nullable
          : data as Object?,
      typeUnderscore:
          typeUnderscore == const $CopyWithPlaceholder() ||
              typeUnderscore == null
          ? _value.typeUnderscore
          // ignore: cast_nullable_to_non_nullable
          : typeUnderscore as IngestRecordsFormatRequestTypeUnderscoreEnum,
      lat: lat == const $CopyWithPlaceholder() || lat == null
          ? _value.lat
          // ignore: cast_nullable_to_non_nullable
          : lat as num,
      lon: lon == const $CopyWithPlaceholder() || lon == null
          ? _value.lon
          // ignore: cast_nullable_to_non_nullable
          : lon as num,
      tst: tst == const $CopyWithPlaceholder()
          ? _value.tst
          // ignore: cast_nullable_to_non_nullable
          : tst as int?,
      acc: acc == const $CopyWithPlaceholder()
          ? _value.acc
          // ignore: cast_nullable_to_non_nullable
          : acc as num?,
      alt: alt == const $CopyWithPlaceholder()
          ? _value.alt
          // ignore: cast_nullable_to_non_nullable
          : alt as num?,
      vel: vel == const $CopyWithPlaceholder()
          ? _value.vel
          // ignore: cast_nullable_to_non_nullable
          : vel as num?,
      cog: cog == const $CopyWithPlaceholder()
          ? _value.cog
          // ignore: cast_nullable_to_non_nullable
          : cog as num?,
      batt: batt == const $CopyWithPlaceholder()
          ? _value.batt
          // ignore: cast_nullable_to_non_nullable
          : batt as num?,
      bs: bs == const $CopyWithPlaceholder()
          ? _value.bs
          // ignore: cast_nullable_to_non_nullable
          : bs as int?,
      conn: conn == const $CopyWithPlaceholder()
          ? _value.conn
          // ignore: cast_nullable_to_non_nullable
          : conn as String?,
      tid: tid == const $CopyWithPlaceholder()
          ? _value.tid
          // ignore: cast_nullable_to_non_nullable
          : tid as String?,
      event: event == const $CopyWithPlaceholder()
          ? _value.event
          // ignore: cast_nullable_to_non_nullable
          : event as String?,
      desc: desc == const $CopyWithPlaceholder()
          ? _value.desc
          // ignore: cast_nullable_to_non_nullable
          : desc as String?,
      wtst: wtst == const $CopyWithPlaceholder()
          ? _value.wtst
          // ignore: cast_nullable_to_non_nullable
          : wtst as int?,
      locations: locations == const $CopyWithPlaceholder() || locations == null
          ? _value.locations
          // ignore: cast_nullable_to_non_nullable
          : locations as List<OverlandBatchLocationsInner>,
      trip: trip == const $CopyWithPlaceholder()
          ? _value.trip
          // ignore: cast_nullable_to_non_nullable
          : trip as Object?,
      current: current == const $CopyWithPlaceholder()
          ? _value.current
          // ignore: cast_nullable_to_non_nullable
          : current as Object?,
      deviceId: deviceId == const $CopyWithPlaceholder() || deviceId == null
          ? _value.deviceId
          // ignore: cast_nullable_to_non_nullable
          : deviceId as String,
      location: location == const $CopyWithPlaceholder() || location == null
          ? _value.location
          // ignore: cast_nullable_to_non_nullable
          : location as OsmAndLocationLocation,
      spd: spd == const $CopyWithPlaceholder()
          ? _value.spd
          // ignore: cast_nullable_to_non_nullable
          : spd as num?,
      dir: dir == const $CopyWithPlaceholder()
          ? _value.dir
          // ignore: cast_nullable_to_non_nullable
          : dir as num?,
      dev: dev == const $CopyWithPlaceholder()
          ? _value.dev
          // ignore: cast_nullable_to_non_nullable
          : dev as String?,
    );
  }
}

extension $IngestRecordsFormatRequestCopyWith on IngestRecordsFormatRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIngestRecordsFormatRequest.copyWith(...)` or `instanceOfIngestRecordsFormatRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IngestRecordsFormatRequestCWProxy get copyWith =>
      _$IngestRecordsFormatRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IngestRecordsFormatRequest _$IngestRecordsFormatRequestFromJson(
  Map<String, dynamic> json,
) => $checkedCreate(
  'IngestRecordsFormatRequest',
  json,
  ($checkedConvert) {
    $checkKeys(
      json,
      requiredKeys: const [
        'type',
        'features',
        'records',
        'version',
        'alerts',
        'specversion',
        'id',
        'source',
        '_type',
        'lat',
        'lon',
        'locations',
        'device_id',
        'location',
      ],
    );
    final val = IngestRecordsFormatRequest(
      type: $checkedConvert('type', (v) => v as String),
      features: $checkedConvert(
        'features',
        (v) => (v as List<dynamic>)
            .map((e) => Feature.fromJson(e as Map<String, dynamic>))
            .toList(),
      ),
      nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      attributions: $checkedConvert(
        'attributions',
        (v) => (v as List<dynamic>?)
            ?.map((e) => Attribution.fromJson(e as Map<String, dynamic>))
            .toList(),
      ),
      watermarkSeq: $checkedConvert(
        'watermarkSeq',
        (v) => (v as num?)?.toInt(),
      ),
      asOfSeq: $checkedConvert('asOfSeq', (v) => (v as num?)?.toInt()),
      asOf: $checkedConvert(
        'asOf',
        (v) => v == null ? null : DateTime.parse(v as String),
      ),
      approximate: $checkedConvert('approximate', (v) => v as bool?),
      horizon: $checkedConvert(
        'horizon',
        (v) => v == null ? null : DateTime.parse(v as String),
      ),
      licenceFiltered: $checkedConvert(
        'licenceFiltered',
        (v) => (v as num?)?.toInt(),
      ),
      records: $checkedConvert(
        'records',
        (v) => (v as List<dynamic>)
            .map(
              (e) =>
                  NormalizedRecordPreview.fromJson(e as Map<String, dynamic>),
            )
            .toList(),
      ),
      version: $checkedConvert(
        'version',
        (v) => $enumDecode(_$IngestRecordsFormatRequestVersionEnumEnumMap, v),
      ),
      groupKey: $checkedConvert('groupKey', (v) => v as String?),
      truncatedAlerts: $checkedConvert(
        'truncatedAlerts',
        (v) => (v as num?)?.toInt(),
      ),
      status: $checkedConvert(
        'status',
        (v) => $enumDecodeNullable(
          _$IngestRecordsFormatRequestStatusEnumEnumMap,
          v,
        ),
      ),
      receiver: $checkedConvert('receiver', (v) => v as String?),
      groupLabels: $checkedConvert(
        'groupLabels',
        (v) => (v as Map<String, dynamic>?)?.map(
          (k, e) => MapEntry(k, e as String),
        ),
      ),
      commonLabels: $checkedConvert(
        'commonLabels',
        (v) => (v as Map<String, dynamic>?)?.map(
          (k, e) => MapEntry(k, e as String),
        ),
      ),
      commonAnnotations: $checkedConvert(
        'commonAnnotations',
        (v) => (v as Map<String, dynamic>?)?.map(
          (k, e) => MapEntry(k, e as String),
        ),
      ),
      externalURL: $checkedConvert('externalURL', (v) => v as String?),
      alerts: $checkedConvert(
        'alerts',
        (v) => (v as List<dynamic>)
            .map(
              (e) => AlertmanagerWebhookAlertsInner.fromJson(
                e as Map<String, dynamic>,
              ),
            )
            .toList(),
      ),
      specversion: $checkedConvert(
        'specversion',
        (v) =>
            $enumDecode(_$IngestRecordsFormatRequestSpecversionEnumEnumMap, v),
      ),
      id: $checkedConvert('id', (v) => v as String),
      source_: $checkedConvert('source', (v) => v as String),
      subject: $checkedConvert('subject', (v) => v as String?),
      time: $checkedConvert('time', (v) => v as String?),
      datacontenttype: $checkedConvert('datacontenttype', (v) => v as String?),
      dataschema: $checkedConvert('dataschema', (v) => v as String?),
      data: $checkedConvert('data', (v) => v),
      typeUnderscore: $checkedConvert(
        '_type',
        (v) => $enumDecode(
          _$IngestRecordsFormatRequestTypeUnderscoreEnumEnumMap,
          v,
        ),
      ),
      lat: $checkedConvert('lat', (v) => v as num),
      lon: $checkedConvert('lon', (v) => v as num),
      tst: $checkedConvert('tst', (v) => (v as num?)?.toInt()),
      acc: $checkedConvert('acc', (v) => v as num?),
      alt: $checkedConvert('alt', (v) => v as num?),
      vel: $checkedConvert('vel', (v) => v as num?),
      cog: $checkedConvert('cog', (v) => v as num?),
      batt: $checkedConvert('batt', (v) => v as num?),
      bs: $checkedConvert('bs', (v) => (v as num?)?.toInt()),
      conn: $checkedConvert('conn', (v) => v as String?),
      tid: $checkedConvert('tid', (v) => v as String?),
      event: $checkedConvert('event', (v) => v as String?),
      desc: $checkedConvert('desc', (v) => v as String?),
      wtst: $checkedConvert('wtst', (v) => (v as num?)?.toInt()),
      locations: $checkedConvert(
        'locations',
        (v) => (v as List<dynamic>)
            .map(
              (e) => OverlandBatchLocationsInner.fromJson(
                e as Map<String, dynamic>,
              ),
            )
            .toList(),
      ),
      trip: $checkedConvert('trip', (v) => v),
      current: $checkedConvert('current', (v) => v),
      deviceId: $checkedConvert('device_id', (v) => v as String),
      location: $checkedConvert(
        'location',
        (v) => OsmAndLocationLocation.fromJson(v as Map<String, dynamic>),
      ),
      spd: $checkedConvert('spd', (v) => v as num?),
      dir: $checkedConvert('dir', (v) => v as num?),
      dev: $checkedConvert('dev', (v) => v as String?),
    );
    return val;
  },
  fieldKeyMap: const {
    'source_': 'source',
    'typeUnderscore': '_type',
    'deviceId': 'device_id',
  },
);

Map<String, dynamic> _$IngestRecordsFormatRequestToJson(
  IngestRecordsFormatRequest instance,
) => <String, dynamic>{
  'type': instance.type,
  'features': instance.features.map((e) => e.toJson()).toList(),
  'nextCursor': ?instance.nextCursor,
  'attributions': ?instance.attributions?.map((e) => e.toJson()).toList(),
  'watermarkSeq': ?instance.watermarkSeq,
  'asOfSeq': ?instance.asOfSeq,
  'asOf': ?instance.asOf?.toIso8601String(),
  'approximate': ?instance.approximate,
  'horizon': ?instance.horizon?.toIso8601String(),
  'licenceFiltered': ?instance.licenceFiltered,
  'records': instance.records.map((e) => e.toJson()).toList(),
  'version': _$IngestRecordsFormatRequestVersionEnumEnumMap[instance.version]!,
  'groupKey': ?instance.groupKey,
  'truncatedAlerts': ?instance.truncatedAlerts,
  'status': ?_$IngestRecordsFormatRequestStatusEnumEnumMap[instance.status],
  'receiver': ?instance.receiver,
  'groupLabels': ?instance.groupLabels,
  'commonLabels': ?instance.commonLabels,
  'commonAnnotations': ?instance.commonAnnotations,
  'externalURL': ?instance.externalURL,
  'alerts': instance.alerts.map((e) => e.toJson()).toList(),
  'specversion':
      _$IngestRecordsFormatRequestSpecversionEnumEnumMap[instance.specversion]!,
  'id': instance.id,
  'source': instance.source_,
  'subject': ?instance.subject,
  'time': ?instance.time,
  'datacontenttype': ?instance.datacontenttype,
  'dataschema': ?instance.dataschema,
  'data': ?instance.data,
  '_type':
      _$IngestRecordsFormatRequestTypeUnderscoreEnumEnumMap[instance
          .typeUnderscore]!,
  'lat': instance.lat,
  'lon': instance.lon,
  'tst': ?instance.tst,
  'acc': ?instance.acc,
  'alt': ?instance.alt,
  'vel': ?instance.vel,
  'cog': ?instance.cog,
  'batt': ?instance.batt,
  'bs': ?instance.bs,
  'conn': ?instance.conn,
  'tid': ?instance.tid,
  'event': ?instance.event,
  'desc': ?instance.desc,
  'wtst': ?instance.wtst,
  'locations': instance.locations.map((e) => e.toJson()).toList(),
  'trip': ?instance.trip,
  'current': ?instance.current,
  'device_id': instance.deviceId,
  'location': instance.location.toJson(),
  'spd': ?instance.spd,
  'dir': ?instance.dir,
  'dev': ?instance.dev,
};

const _$IngestRecordsFormatRequestVersionEnumEnumMap = {
  IngestRecordsFormatRequestVersionEnum.n4: '4',
};

const _$IngestRecordsFormatRequestStatusEnumEnumMap = {
  IngestRecordsFormatRequestStatusEnum.firing: 'firing',
  IngestRecordsFormatRequestStatusEnum.resolved: 'resolved',
};

const _$IngestRecordsFormatRequestSpecversionEnumEnumMap = {
  IngestRecordsFormatRequestSpecversionEnum.n1period0: '1.0',
};

const _$IngestRecordsFormatRequestTypeUnderscoreEnumEnumMap = {
  IngestRecordsFormatRequestTypeUnderscoreEnum.location: 'location',
  IngestRecordsFormatRequestTypeUnderscoreEnum.transition: 'transition',
  IngestRecordsFormatRequestTypeUnderscoreEnum.waypoint: 'waypoint',
  IngestRecordsFormatRequestTypeUnderscoreEnum.lwt: 'lwt',
  IngestRecordsFormatRequestTypeUnderscoreEnum.card: 'card',
  IngestRecordsFormatRequestTypeUnderscoreEnum.status: 'status',
  IngestRecordsFormatRequestTypeUnderscoreEnum.encrypted: 'encrypted',
};
