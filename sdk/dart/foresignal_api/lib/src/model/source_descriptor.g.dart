// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'source_descriptor.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SourceDescriptorCWProxy {
  SourceDescriptor id(String id);

  SourceDescriptor name(String name);

  SourceDescriptor description(String? description);

  SourceDescriptor kinds(List<String> kinds);

  SourceDescriptor homepage(String? homepage);

  SourceDescriptor attribution(String? attribution);

  SourceDescriptor mode(SourceDescriptorModeEnum mode);

  SourceDescriptor custom(bool? custom);

  SourceDescriptor credentialFields(List<CredentialField> credentialFields);

  SourceDescriptor params(List<ParamSpec> params);

  SourceDescriptor minInterval(String? minInterval);

  SourceDescriptor defaultInterval(String? defaultInterval);

  SourceDescriptor defaultBudget(Budget defaultBudget);

  SourceDescriptor recordTtl(String? recordTtl);

  SourceDescriptor zeroResultsNormal(bool? zeroResultsNormal);

  SourceDescriptor tileProperties(List<String>? tileProperties);

  SourceDescriptor searchFields(List<String>? searchFields);

  SourceDescriptor embedFields(List<String>? embedFields);

  SourceDescriptor rankField(String? rankField);

  SourceDescriptor licence(String? licence);

  SourceDescriptor termsUrl(String? termsUrl);

  SourceDescriptor historyTier(HistoryTier? historyTier);

  SourceDescriptor observedAtMode(
    SourceDescriptorObservedAtModeEnum? observedAtMode,
  );

  SourceDescriptor ttlAnchor(SourceDescriptorTtlAnchorEnum? ttlAnchor);

  SourceDescriptor defaultIngestPolicy(IngestPolicy? defaultIngestPolicy);

  SourceDescriptor volatileFields(List<String>? volatileFields);

  SourceDescriptor significantFields(List<String>? significantFields);

  SourceDescriptor motion(MotionSpec? motion);

  SourceDescriptor thresholds(List<FieldThreshold>? thresholds);

  SourceDescriptor defaultRetention(String? defaultRetention);

  SourceDescriptor kindRetention(Map<String, String>? kindRetention);

  SourceDescriptor skewTolerance(String? skewTolerance);

  SourceDescriptor allowNullIsland(bool? allowNullIsland);

  SourceDescriptor products(List<String>? products);

  SourceDescriptor titleField(String? titleField);

  SourceDescriptor entityKeys(Map<String, String>? entityKeys);

  SourceDescriptor fieldSpecs(Map<String, FieldSpec>? fieldSpecs);

  SourceDescriptor kindDisplay(Map<String, KindDisplay>? kindDisplay);

  SourceDescriptor aggBelowZoom(int? aggBelowZoom);

  SourceDescriptor startField(String? startField);

  SourceDescriptor endField(String? endField);

  SourceDescriptor private(bool? private);

  SourceDescriptor privatePrecisionDecimals(int? privatePrecisionDecimals);

  SourceDescriptor acceptsRemoteMedia(bool? acceptsRemoteMedia);

  SourceDescriptor egress(SourceDescriptorEgressEnum? egress);

  SourceDescriptor egressOverridable(bool? egressOverridable);

  SourceDescriptor llmNotice(String? llmNotice);

  SourceDescriptor licenceTerms(LicenceTerms? licenceTerms);

  SourceDescriptor reliability(SourceDescriptorReliabilityEnum? reliability);

  SourceDescriptor derivedFrom(List<String>? derivedFrom);

  SourceDescriptor originator(String? originator);

  SourceDescriptor upstreamRefFields(List<String>? upstreamRefFields);

  SourceDescriptor observability(DeclaredObservability? observability);

  SourceDescriptor access(SourceAccess? access);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SourceDescriptor(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SourceDescriptor(...).copyWith(id: 12, name: "My name")
  /// ```
  SourceDescriptor call({
    String id,
    String name,
    String? description,
    List<String> kinds,
    String? homepage,
    String? attribution,
    SourceDescriptorModeEnum mode,
    bool? custom,
    List<CredentialField> credentialFields,
    List<ParamSpec> params,
    String? minInterval,
    String? defaultInterval,
    Budget defaultBudget,
    String? recordTtl,
    bool? zeroResultsNormal,
    List<String>? tileProperties,
    List<String>? searchFields,
    List<String>? embedFields,
    String? rankField,
    String? licence,
    String? termsUrl,
    HistoryTier? historyTier,
    SourceDescriptorObservedAtModeEnum? observedAtMode,
    SourceDescriptorTtlAnchorEnum? ttlAnchor,
    IngestPolicy? defaultIngestPolicy,
    List<String>? volatileFields,
    List<String>? significantFields,
    MotionSpec? motion,
    List<FieldThreshold>? thresholds,
    String? defaultRetention,
    Map<String, String>? kindRetention,
    String? skewTolerance,
    bool? allowNullIsland,
    List<String>? products,
    String? titleField,
    Map<String, String>? entityKeys,
    Map<String, FieldSpec>? fieldSpecs,
    Map<String, KindDisplay>? kindDisplay,
    int? aggBelowZoom,
    String? startField,
    String? endField,
    bool? private,
    int? privatePrecisionDecimals,
    bool? acceptsRemoteMedia,
    SourceDescriptorEgressEnum? egress,
    bool? egressOverridable,
    String? llmNotice,
    LicenceTerms? licenceTerms,
    SourceDescriptorReliabilityEnum? reliability,
    List<String>? derivedFrom,
    String? originator,
    List<String>? upstreamRefFields,
    DeclaredObservability? observability,
    SourceAccess? access,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSourceDescriptor.copyWith(...)` or call `instanceOfSourceDescriptor.copyWith.fieldName(value)` for a single field.
class _$SourceDescriptorCWProxyImpl implements _$SourceDescriptorCWProxy {
  const _$SourceDescriptorCWProxyImpl(this._value);

  final SourceDescriptor _value;

  @override
  SourceDescriptor id(String id) => call(id: id);

  @override
  SourceDescriptor name(String name) => call(name: name);

  @override
  SourceDescriptor description(String? description) =>
      call(description: description);

  @override
  SourceDescriptor kinds(List<String> kinds) => call(kinds: kinds);

  @override
  SourceDescriptor homepage(String? homepage) => call(homepage: homepage);

  @override
  SourceDescriptor attribution(String? attribution) =>
      call(attribution: attribution);

  @override
  SourceDescriptor mode(SourceDescriptorModeEnum mode) => call(mode: mode);

  @override
  SourceDescriptor custom(bool? custom) => call(custom: custom);

  @override
  SourceDescriptor credentialFields(List<CredentialField> credentialFields) =>
      call(credentialFields: credentialFields);

  @override
  SourceDescriptor params(List<ParamSpec> params) => call(params: params);

  @override
  SourceDescriptor minInterval(String? minInterval) =>
      call(minInterval: minInterval);

  @override
  SourceDescriptor defaultInterval(String? defaultInterval) =>
      call(defaultInterval: defaultInterval);

  @override
  SourceDescriptor defaultBudget(Budget defaultBudget) =>
      call(defaultBudget: defaultBudget);

  @override
  SourceDescriptor recordTtl(String? recordTtl) => call(recordTtl: recordTtl);

  @override
  SourceDescriptor zeroResultsNormal(bool? zeroResultsNormal) =>
      call(zeroResultsNormal: zeroResultsNormal);

  @override
  SourceDescriptor tileProperties(List<String>? tileProperties) =>
      call(tileProperties: tileProperties);

  @override
  SourceDescriptor searchFields(List<String>? searchFields) =>
      call(searchFields: searchFields);

  @override
  SourceDescriptor embedFields(List<String>? embedFields) =>
      call(embedFields: embedFields);

  @override
  SourceDescriptor rankField(String? rankField) => call(rankField: rankField);

  @override
  SourceDescriptor licence(String? licence) => call(licence: licence);

  @override
  SourceDescriptor termsUrl(String? termsUrl) => call(termsUrl: termsUrl);

  @override
  SourceDescriptor historyTier(HistoryTier? historyTier) =>
      call(historyTier: historyTier);

  @override
  SourceDescriptor observedAtMode(
    SourceDescriptorObservedAtModeEnum? observedAtMode,
  ) => call(observedAtMode: observedAtMode);

  @override
  SourceDescriptor ttlAnchor(SourceDescriptorTtlAnchorEnum? ttlAnchor) =>
      call(ttlAnchor: ttlAnchor);

  @override
  SourceDescriptor defaultIngestPolicy(IngestPolicy? defaultIngestPolicy) =>
      call(defaultIngestPolicy: defaultIngestPolicy);

  @override
  SourceDescriptor volatileFields(List<String>? volatileFields) =>
      call(volatileFields: volatileFields);

  @override
  SourceDescriptor significantFields(List<String>? significantFields) =>
      call(significantFields: significantFields);

  @override
  SourceDescriptor motion(MotionSpec? motion) => call(motion: motion);

  @override
  SourceDescriptor thresholds(List<FieldThreshold>? thresholds) =>
      call(thresholds: thresholds);

  @override
  SourceDescriptor defaultRetention(String? defaultRetention) =>
      call(defaultRetention: defaultRetention);

  @override
  SourceDescriptor kindRetention(Map<String, String>? kindRetention) =>
      call(kindRetention: kindRetention);

  @override
  SourceDescriptor skewTolerance(String? skewTolerance) =>
      call(skewTolerance: skewTolerance);

  @override
  SourceDescriptor allowNullIsland(bool? allowNullIsland) =>
      call(allowNullIsland: allowNullIsland);

  @override
  SourceDescriptor products(List<String>? products) => call(products: products);

  @override
  SourceDescriptor titleField(String? titleField) =>
      call(titleField: titleField);

  @override
  SourceDescriptor entityKeys(Map<String, String>? entityKeys) =>
      call(entityKeys: entityKeys);

  @override
  SourceDescriptor fieldSpecs(Map<String, FieldSpec>? fieldSpecs) =>
      call(fieldSpecs: fieldSpecs);

  @override
  SourceDescriptor kindDisplay(Map<String, KindDisplay>? kindDisplay) =>
      call(kindDisplay: kindDisplay);

  @override
  SourceDescriptor aggBelowZoom(int? aggBelowZoom) =>
      call(aggBelowZoom: aggBelowZoom);

  @override
  SourceDescriptor startField(String? startField) =>
      call(startField: startField);

  @override
  SourceDescriptor endField(String? endField) => call(endField: endField);

  @override
  SourceDescriptor private(bool? private) => call(private: private);

  @override
  SourceDescriptor privatePrecisionDecimals(int? privatePrecisionDecimals) =>
      call(privatePrecisionDecimals: privatePrecisionDecimals);

  @override
  SourceDescriptor acceptsRemoteMedia(bool? acceptsRemoteMedia) =>
      call(acceptsRemoteMedia: acceptsRemoteMedia);

  @override
  SourceDescriptor egress(SourceDescriptorEgressEnum? egress) =>
      call(egress: egress);

  @override
  SourceDescriptor egressOverridable(bool? egressOverridable) =>
      call(egressOverridable: egressOverridable);

  @override
  SourceDescriptor llmNotice(String? llmNotice) => call(llmNotice: llmNotice);

  @override
  SourceDescriptor licenceTerms(LicenceTerms? licenceTerms) =>
      call(licenceTerms: licenceTerms);

  @override
  SourceDescriptor reliability(SourceDescriptorReliabilityEnum? reliability) =>
      call(reliability: reliability);

  @override
  SourceDescriptor derivedFrom(List<String>? derivedFrom) =>
      call(derivedFrom: derivedFrom);

  @override
  SourceDescriptor originator(String? originator) =>
      call(originator: originator);

  @override
  SourceDescriptor upstreamRefFields(List<String>? upstreamRefFields) =>
      call(upstreamRefFields: upstreamRefFields);

  @override
  SourceDescriptor observability(DeclaredObservability? observability) =>
      call(observability: observability);

  @override
  SourceDescriptor access(SourceAccess? access) => call(access: access);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SourceDescriptor(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SourceDescriptor(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SourceDescriptor call({
    Object? id = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? kinds = const $CopyWithPlaceholder(),
    Object? homepage = const $CopyWithPlaceholder(),
    Object? attribution = const $CopyWithPlaceholder(),
    Object? mode = const $CopyWithPlaceholder(),
    Object? custom = const $CopyWithPlaceholder(),
    Object? credentialFields = const $CopyWithPlaceholder(),
    Object? params = const $CopyWithPlaceholder(),
    Object? minInterval = const $CopyWithPlaceholder(),
    Object? defaultInterval = const $CopyWithPlaceholder(),
    Object? defaultBudget = const $CopyWithPlaceholder(),
    Object? recordTtl = const $CopyWithPlaceholder(),
    Object? zeroResultsNormal = const $CopyWithPlaceholder(),
    Object? tileProperties = const $CopyWithPlaceholder(),
    Object? searchFields = const $CopyWithPlaceholder(),
    Object? embedFields = const $CopyWithPlaceholder(),
    Object? rankField = const $CopyWithPlaceholder(),
    Object? licence = const $CopyWithPlaceholder(),
    Object? termsUrl = const $CopyWithPlaceholder(),
    Object? historyTier = const $CopyWithPlaceholder(),
    Object? observedAtMode = const $CopyWithPlaceholder(),
    Object? ttlAnchor = const $CopyWithPlaceholder(),
    Object? defaultIngestPolicy = const $CopyWithPlaceholder(),
    Object? volatileFields = const $CopyWithPlaceholder(),
    Object? significantFields = const $CopyWithPlaceholder(),
    Object? motion = const $CopyWithPlaceholder(),
    Object? thresholds = const $CopyWithPlaceholder(),
    Object? defaultRetention = const $CopyWithPlaceholder(),
    Object? kindRetention = const $CopyWithPlaceholder(),
    Object? skewTolerance = const $CopyWithPlaceholder(),
    Object? allowNullIsland = const $CopyWithPlaceholder(),
    Object? products = const $CopyWithPlaceholder(),
    Object? titleField = const $CopyWithPlaceholder(),
    Object? entityKeys = const $CopyWithPlaceholder(),
    Object? fieldSpecs = const $CopyWithPlaceholder(),
    Object? kindDisplay = const $CopyWithPlaceholder(),
    Object? aggBelowZoom = const $CopyWithPlaceholder(),
    Object? startField = const $CopyWithPlaceholder(),
    Object? endField = const $CopyWithPlaceholder(),
    Object? private = const $CopyWithPlaceholder(),
    Object? privatePrecisionDecimals = const $CopyWithPlaceholder(),
    Object? acceptsRemoteMedia = const $CopyWithPlaceholder(),
    Object? egress = const $CopyWithPlaceholder(),
    Object? egressOverridable = const $CopyWithPlaceholder(),
    Object? llmNotice = const $CopyWithPlaceholder(),
    Object? licenceTerms = const $CopyWithPlaceholder(),
    Object? reliability = const $CopyWithPlaceholder(),
    Object? derivedFrom = const $CopyWithPlaceholder(),
    Object? originator = const $CopyWithPlaceholder(),
    Object? upstreamRefFields = const $CopyWithPlaceholder(),
    Object? observability = const $CopyWithPlaceholder(),
    Object? access = const $CopyWithPlaceholder(),
  }) {
    return SourceDescriptor(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      description: description == const $CopyWithPlaceholder()
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String?,
      kinds: kinds == const $CopyWithPlaceholder() || kinds == null
          ? _value.kinds
          // ignore: cast_nullable_to_non_nullable
          : kinds as List<String>,
      homepage: homepage == const $CopyWithPlaceholder()
          ? _value.homepage
          // ignore: cast_nullable_to_non_nullable
          : homepage as String?,
      attribution: attribution == const $CopyWithPlaceholder()
          ? _value.attribution
          // ignore: cast_nullable_to_non_nullable
          : attribution as String?,
      mode: mode == const $CopyWithPlaceholder() || mode == null
          ? _value.mode
          // ignore: cast_nullable_to_non_nullable
          : mode as SourceDescriptorModeEnum,
      custom: custom == const $CopyWithPlaceholder()
          ? _value.custom
          // ignore: cast_nullable_to_non_nullable
          : custom as bool?,
      credentialFields:
          credentialFields == const $CopyWithPlaceholder() ||
              credentialFields == null
          ? _value.credentialFields
          // ignore: cast_nullable_to_non_nullable
          : credentialFields as List<CredentialField>,
      params: params == const $CopyWithPlaceholder() || params == null
          ? _value.params
          // ignore: cast_nullable_to_non_nullable
          : params as List<ParamSpec>,
      minInterval: minInterval == const $CopyWithPlaceholder()
          ? _value.minInterval
          // ignore: cast_nullable_to_non_nullable
          : minInterval as String?,
      defaultInterval: defaultInterval == const $CopyWithPlaceholder()
          ? _value.defaultInterval
          // ignore: cast_nullable_to_non_nullable
          : defaultInterval as String?,
      defaultBudget:
          defaultBudget == const $CopyWithPlaceholder() || defaultBudget == null
          ? _value.defaultBudget
          // ignore: cast_nullable_to_non_nullable
          : defaultBudget as Budget,
      recordTtl: recordTtl == const $CopyWithPlaceholder()
          ? _value.recordTtl
          // ignore: cast_nullable_to_non_nullable
          : recordTtl as String?,
      zeroResultsNormal: zeroResultsNormal == const $CopyWithPlaceholder()
          ? _value.zeroResultsNormal
          // ignore: cast_nullable_to_non_nullable
          : zeroResultsNormal as bool?,
      tileProperties: tileProperties == const $CopyWithPlaceholder()
          ? _value.tileProperties
          // ignore: cast_nullable_to_non_nullable
          : tileProperties as List<String>?,
      searchFields: searchFields == const $CopyWithPlaceholder()
          ? _value.searchFields
          // ignore: cast_nullable_to_non_nullable
          : searchFields as List<String>?,
      embedFields: embedFields == const $CopyWithPlaceholder()
          ? _value.embedFields
          // ignore: cast_nullable_to_non_nullable
          : embedFields as List<String>?,
      rankField: rankField == const $CopyWithPlaceholder()
          ? _value.rankField
          // ignore: cast_nullable_to_non_nullable
          : rankField as String?,
      licence: licence == const $CopyWithPlaceholder()
          ? _value.licence
          // ignore: cast_nullable_to_non_nullable
          : licence as String?,
      termsUrl: termsUrl == const $CopyWithPlaceholder()
          ? _value.termsUrl
          // ignore: cast_nullable_to_non_nullable
          : termsUrl as String?,
      historyTier: historyTier == const $CopyWithPlaceholder()
          ? _value.historyTier
          // ignore: cast_nullable_to_non_nullable
          : historyTier as HistoryTier?,
      observedAtMode: observedAtMode == const $CopyWithPlaceholder()
          ? _value.observedAtMode
          // ignore: cast_nullable_to_non_nullable
          : observedAtMode as SourceDescriptorObservedAtModeEnum?,
      ttlAnchor: ttlAnchor == const $CopyWithPlaceholder()
          ? _value.ttlAnchor
          // ignore: cast_nullable_to_non_nullable
          : ttlAnchor as SourceDescriptorTtlAnchorEnum?,
      defaultIngestPolicy: defaultIngestPolicy == const $CopyWithPlaceholder()
          ? _value.defaultIngestPolicy
          // ignore: cast_nullable_to_non_nullable
          : defaultIngestPolicy as IngestPolicy?,
      volatileFields: volatileFields == const $CopyWithPlaceholder()
          ? _value.volatileFields
          // ignore: cast_nullable_to_non_nullable
          : volatileFields as List<String>?,
      significantFields: significantFields == const $CopyWithPlaceholder()
          ? _value.significantFields
          // ignore: cast_nullable_to_non_nullable
          : significantFields as List<String>?,
      motion: motion == const $CopyWithPlaceholder()
          ? _value.motion
          // ignore: cast_nullable_to_non_nullable
          : motion as MotionSpec?,
      thresholds: thresholds == const $CopyWithPlaceholder()
          ? _value.thresholds
          // ignore: cast_nullable_to_non_nullable
          : thresholds as List<FieldThreshold>?,
      defaultRetention: defaultRetention == const $CopyWithPlaceholder()
          ? _value.defaultRetention
          // ignore: cast_nullable_to_non_nullable
          : defaultRetention as String?,
      kindRetention: kindRetention == const $CopyWithPlaceholder()
          ? _value.kindRetention
          // ignore: cast_nullable_to_non_nullable
          : kindRetention as Map<String, String>?,
      skewTolerance: skewTolerance == const $CopyWithPlaceholder()
          ? _value.skewTolerance
          // ignore: cast_nullable_to_non_nullable
          : skewTolerance as String?,
      allowNullIsland: allowNullIsland == const $CopyWithPlaceholder()
          ? _value.allowNullIsland
          // ignore: cast_nullable_to_non_nullable
          : allowNullIsland as bool?,
      products: products == const $CopyWithPlaceholder()
          ? _value.products
          // ignore: cast_nullable_to_non_nullable
          : products as List<String>?,
      titleField: titleField == const $CopyWithPlaceholder()
          ? _value.titleField
          // ignore: cast_nullable_to_non_nullable
          : titleField as String?,
      entityKeys: entityKeys == const $CopyWithPlaceholder()
          ? _value.entityKeys
          // ignore: cast_nullable_to_non_nullable
          : entityKeys as Map<String, String>?,
      fieldSpecs: fieldSpecs == const $CopyWithPlaceholder()
          ? _value.fieldSpecs
          // ignore: cast_nullable_to_non_nullable
          : fieldSpecs as Map<String, FieldSpec>?,
      kindDisplay: kindDisplay == const $CopyWithPlaceholder()
          ? _value.kindDisplay
          // ignore: cast_nullable_to_non_nullable
          : kindDisplay as Map<String, KindDisplay>?,
      aggBelowZoom: aggBelowZoom == const $CopyWithPlaceholder()
          ? _value.aggBelowZoom
          // ignore: cast_nullable_to_non_nullable
          : aggBelowZoom as int?,
      startField: startField == const $CopyWithPlaceholder()
          ? _value.startField
          // ignore: cast_nullable_to_non_nullable
          : startField as String?,
      endField: endField == const $CopyWithPlaceholder()
          ? _value.endField
          // ignore: cast_nullable_to_non_nullable
          : endField as String?,
      private: private == const $CopyWithPlaceholder()
          ? _value.private
          // ignore: cast_nullable_to_non_nullable
          : private as bool?,
      privatePrecisionDecimals:
          privatePrecisionDecimals == const $CopyWithPlaceholder()
          ? _value.privatePrecisionDecimals
          // ignore: cast_nullable_to_non_nullable
          : privatePrecisionDecimals as int?,
      acceptsRemoteMedia: acceptsRemoteMedia == const $CopyWithPlaceholder()
          ? _value.acceptsRemoteMedia
          // ignore: cast_nullable_to_non_nullable
          : acceptsRemoteMedia as bool?,
      egress: egress == const $CopyWithPlaceholder()
          ? _value.egress
          // ignore: cast_nullable_to_non_nullable
          : egress as SourceDescriptorEgressEnum?,
      egressOverridable: egressOverridable == const $CopyWithPlaceholder()
          ? _value.egressOverridable
          // ignore: cast_nullable_to_non_nullable
          : egressOverridable as bool?,
      llmNotice: llmNotice == const $CopyWithPlaceholder()
          ? _value.llmNotice
          // ignore: cast_nullable_to_non_nullable
          : llmNotice as String?,
      licenceTerms: licenceTerms == const $CopyWithPlaceholder()
          ? _value.licenceTerms
          // ignore: cast_nullable_to_non_nullable
          : licenceTerms as LicenceTerms?,
      reliability: reliability == const $CopyWithPlaceholder()
          ? _value.reliability
          // ignore: cast_nullable_to_non_nullable
          : reliability as SourceDescriptorReliabilityEnum?,
      derivedFrom: derivedFrom == const $CopyWithPlaceholder()
          ? _value.derivedFrom
          // ignore: cast_nullable_to_non_nullable
          : derivedFrom as List<String>?,
      originator: originator == const $CopyWithPlaceholder()
          ? _value.originator
          // ignore: cast_nullable_to_non_nullable
          : originator as String?,
      upstreamRefFields: upstreamRefFields == const $CopyWithPlaceholder()
          ? _value.upstreamRefFields
          // ignore: cast_nullable_to_non_nullable
          : upstreamRefFields as List<String>?,
      observability: observability == const $CopyWithPlaceholder()
          ? _value.observability
          // ignore: cast_nullable_to_non_nullable
          : observability as DeclaredObservability?,
      access: access == const $CopyWithPlaceholder()
          ? _value.access
          // ignore: cast_nullable_to_non_nullable
          : access as SourceAccess?,
    );
  }
}

extension $SourceDescriptorCopyWith on SourceDescriptor {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSourceDescriptor.copyWith(...)` or `instanceOfSourceDescriptor.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SourceDescriptorCWProxy get copyWith => _$SourceDescriptorCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SourceDescriptor _$SourceDescriptorFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SourceDescriptor', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'name',
      'kinds',
      'mode',
      'credentialFields',
      'params',
      'defaultBudget',
    ],
  );
  final val = SourceDescriptor(
    id: $checkedConvert('id', (v) => v as String),
    name: $checkedConvert('name', (v) => v as String),
    description: $checkedConvert('description', (v) => v as String?),
    kinds: $checkedConvert(
      'kinds',
      (v) => (v as List<dynamic>).map((e) => e as String).toList(),
    ),
    homepage: $checkedConvert('homepage', (v) => v as String?),
    attribution: $checkedConvert('attribution', (v) => v as String?),
    mode: $checkedConvert(
      'mode',
      (v) => $enumDecode(_$SourceDescriptorModeEnumEnumMap, v),
    ),
    custom: $checkedConvert('custom', (v) => v as bool?),
    credentialFields: $checkedConvert(
      'credentialFields',
      (v) => (v as List<dynamic>)
          .map((e) => CredentialField.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    params: $checkedConvert(
      'params',
      (v) => (v as List<dynamic>)
          .map((e) => ParamSpec.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    minInterval: $checkedConvert('minInterval', (v) => v as String?),
    defaultInterval: $checkedConvert('defaultInterval', (v) => v as String?),
    defaultBudget: $checkedConvert(
      'defaultBudget',
      (v) => Budget.fromJson(v as Map<String, dynamic>),
    ),
    recordTtl: $checkedConvert('recordTtl', (v) => v as String?),
    zeroResultsNormal: $checkedConvert('zeroResultsNormal', (v) => v as bool?),
    tileProperties: $checkedConvert(
      'tileProperties',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    searchFields: $checkedConvert(
      'searchFields',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    embedFields: $checkedConvert(
      'embedFields',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    rankField: $checkedConvert('rankField', (v) => v as String?),
    licence: $checkedConvert('licence', (v) => v as String?),
    termsUrl: $checkedConvert('termsUrl', (v) => v as String?),
    historyTier: $checkedConvert(
      'historyTier',
      (v) => $enumDecodeNullable(_$HistoryTierEnumMap, v),
    ),
    observedAtMode: $checkedConvert(
      'observedAtMode',
      (v) =>
          $enumDecodeNullable(_$SourceDescriptorObservedAtModeEnumEnumMap, v),
    ),
    ttlAnchor: $checkedConvert(
      'ttlAnchor',
      (v) => $enumDecodeNullable(_$SourceDescriptorTtlAnchorEnumEnumMap, v),
    ),
    defaultIngestPolicy: $checkedConvert(
      'defaultIngestPolicy',
      (v) =>
          v == null ? null : IngestPolicy.fromJson(v as Map<String, dynamic>),
    ),
    volatileFields: $checkedConvert(
      'volatileFields',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    significantFields: $checkedConvert(
      'significantFields',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    motion: $checkedConvert(
      'motion',
      (v) => v == null ? null : MotionSpec.fromJson(v as Map<String, dynamic>),
    ),
    thresholds: $checkedConvert(
      'thresholds',
      (v) => (v as List<dynamic>?)
          ?.map((e) => FieldThreshold.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    defaultRetention: $checkedConvert('defaultRetention', (v) => v as String?),
    kindRetention: $checkedConvert(
      'kindRetention',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as String)),
    ),
    skewTolerance: $checkedConvert('skewTolerance', (v) => v as String?),
    allowNullIsland: $checkedConvert(
      'allowNullIsland',
      (v) => v as bool? ?? false,
    ),
    products: $checkedConvert(
      'products',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    titleField: $checkedConvert('titleField', (v) => v as String?),
    entityKeys: $checkedConvert(
      'entityKeys',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as String)),
    ),
    fieldSpecs: $checkedConvert(
      'fieldSpecs',
      (v) => (v as Map<String, dynamic>?)?.map(
        (k, e) => MapEntry(k, FieldSpec.fromJson(e as Map<String, dynamic>)),
      ),
    ),
    kindDisplay: $checkedConvert(
      'kindDisplay',
      (v) => (v as Map<String, dynamic>?)?.map(
        (k, e) => MapEntry(k, KindDisplay.fromJson(e as Map<String, dynamic>)),
      ),
    ),
    aggBelowZoom: $checkedConvert('aggBelowZoom', (v) => (v as num?)?.toInt()),
    startField: $checkedConvert('startField', (v) => v as String?),
    endField: $checkedConvert('endField', (v) => v as String?),
    private: $checkedConvert('private', (v) => v as bool? ?? false),
    privatePrecisionDecimals: $checkedConvert(
      'privatePrecisionDecimals',
      (v) => (v as num?)?.toInt() ?? 4,
    ),
    acceptsRemoteMedia: $checkedConvert(
      'acceptsRemoteMedia',
      (v) => v as bool?,
    ),
    egress: $checkedConvert(
      'egress',
      (v) => $enumDecodeNullable(_$SourceDescriptorEgressEnumEnumMap, v),
    ),
    egressOverridable: $checkedConvert(
      'egressOverridable',
      (v) => v as bool? ?? false,
    ),
    llmNotice: $checkedConvert('llmNotice', (v) => v as String?),
    licenceTerms: $checkedConvert(
      'licenceTerms',
      (v) =>
          v == null ? null : LicenceTerms.fromJson(v as Map<String, dynamic>),
    ),
    reliability: $checkedConvert(
      'reliability',
      (v) => $enumDecodeNullable(_$SourceDescriptorReliabilityEnumEnumMap, v),
    ),
    derivedFrom: $checkedConvert(
      'derivedFrom',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    originator: $checkedConvert('originator', (v) => v as String?),
    upstreamRefFields: $checkedConvert(
      'upstreamRefFields',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    observability: $checkedConvert(
      'observability',
      (v) => v == null
          ? null
          : DeclaredObservability.fromJson(v as Map<String, dynamic>),
    ),
    access: $checkedConvert(
      'access',
      (v) =>
          v == null ? null : SourceAccess.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$SourceDescriptorToJson(
  SourceDescriptor instance,
) => <String, dynamic>{
  'id': instance.id,
  'name': instance.name,
  'description': ?instance.description,
  'kinds': instance.kinds,
  'homepage': ?instance.homepage,
  'attribution': ?instance.attribution,
  'mode': _$SourceDescriptorModeEnumEnumMap[instance.mode]!,
  'custom': ?instance.custom,
  'credentialFields': instance.credentialFields.map((e) => e.toJson()).toList(),
  'params': instance.params.map((e) => e.toJson()).toList(),
  'minInterval': ?instance.minInterval,
  'defaultInterval': ?instance.defaultInterval,
  'defaultBudget': instance.defaultBudget.toJson(),
  'recordTtl': ?instance.recordTtl,
  'zeroResultsNormal': ?instance.zeroResultsNormal,
  'tileProperties': ?instance.tileProperties,
  'searchFields': ?instance.searchFields,
  'embedFields': ?instance.embedFields,
  'rankField': ?instance.rankField,
  'licence': ?instance.licence,
  'termsUrl': ?instance.termsUrl,
  'historyTier': ?_$HistoryTierEnumMap[instance.historyTier],
  'observedAtMode':
      ?_$SourceDescriptorObservedAtModeEnumEnumMap[instance.observedAtMode],
  'ttlAnchor': ?_$SourceDescriptorTtlAnchorEnumEnumMap[instance.ttlAnchor],
  'defaultIngestPolicy': ?instance.defaultIngestPolicy?.toJson(),
  'volatileFields': ?instance.volatileFields,
  'significantFields': ?instance.significantFields,
  'motion': ?instance.motion?.toJson(),
  'thresholds': ?instance.thresholds?.map((e) => e.toJson()).toList(),
  'defaultRetention': ?instance.defaultRetention,
  'kindRetention': ?instance.kindRetention,
  'skewTolerance': ?instance.skewTolerance,
  'allowNullIsland': ?instance.allowNullIsland,
  'products': ?instance.products,
  'titleField': ?instance.titleField,
  'entityKeys': ?instance.entityKeys,
  'fieldSpecs': ?instance.fieldSpecs?.map((k, e) => MapEntry(k, e.toJson())),
  'kindDisplay': ?instance.kindDisplay?.map((k, e) => MapEntry(k, e.toJson())),
  'aggBelowZoom': ?instance.aggBelowZoom,
  'startField': ?instance.startField,
  'endField': ?instance.endField,
  'private': ?instance.private,
  'privatePrecisionDecimals': ?instance.privatePrecisionDecimals,
  'acceptsRemoteMedia': ?instance.acceptsRemoteMedia,
  'egress': ?_$SourceDescriptorEgressEnumEnumMap[instance.egress],
  'egressOverridable': ?instance.egressOverridable,
  'llmNotice': ?instance.llmNotice,
  'licenceTerms': ?instance.licenceTerms?.toJson(),
  'reliability':
      ?_$SourceDescriptorReliabilityEnumEnumMap[instance.reliability],
  'derivedFrom': ?instance.derivedFrom,
  'originator': ?instance.originator,
  'upstreamRefFields': ?instance.upstreamRefFields,
  'observability': ?instance.observability?.toJson(),
  'access': ?instance.access?.toJson(),
};

const _$SourceDescriptorModeEnumEnumMap = {
  SourceDescriptorModeEnum.polling: 'polling',
  SourceDescriptorModeEnum.streaming: 'streaming',
  SourceDescriptorModeEnum.push: 'push',
  SourceDescriptorModeEnum.derived: 'derived',
};

const _$HistoryTierEnumMap = {
  HistoryTier.STANDARD: 'STANDARD',
  HistoryTier.TRACK: 'TRACK',
};

const _$SourceDescriptorObservedAtModeEnumEnumMap = {
  SourceDescriptorObservedAtModeEnum.UPSTREAM: 'UPSTREAM',
  SourceDescriptorObservedAtModeEnum.RECEIVED: 'RECEIVED',
};

const _$SourceDescriptorTtlAnchorEnumEnumMap = {
  SourceDescriptorTtlAnchorEnum.OBSERVED: 'OBSERVED',
  SourceDescriptorTtlAnchorEnum.RECEIVED: 'RECEIVED',
};

const _$SourceDescriptorEgressEnumEnumMap = {
  SourceDescriptorEgressEnum.OPEN: 'OPEN',
  SourceDescriptorEgressEnum.LOCAL_ONLY: 'LOCAL_ONLY',
};

const _$SourceDescriptorReliabilityEnumEnumMap = {
  SourceDescriptorReliabilityEnum.A: 'A',
  SourceDescriptorReliabilityEnum.B: 'B',
  SourceDescriptorReliabilityEnum.C: 'C',
  SourceDescriptorReliabilityEnum.D: 'D',
  SourceDescriptorReliabilityEnum.E: 'E',
  SourceDescriptorReliabilityEnum.F: 'F',
};
