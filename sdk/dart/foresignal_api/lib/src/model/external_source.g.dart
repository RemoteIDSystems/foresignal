// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'external_source.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ExternalSourceCWProxy {
  ExternalSource sourceId(String sourceId);

  ExternalSource state(ExternalSourceStateEnum state);

  ExternalSource registeredVia(ExternalSourceRegisteredViaEnum? registeredVia);

  ExternalSource manifest(EdpManifest manifest);

  ExternalSource manifestSha256(String? manifestSha256);

  ExternalSource access(SourceAccess? access);

  ExternalSource egress(String? egress);

  ExternalSource approvedBy(String? approvedBy);

  ExternalSource approvedAt(DateTime? approvedAt);

  ExternalSource createdAt(DateTime createdAt);

  ExternalSource lastItemAt(DateTime? lastItemAt);

  ExternalSource pull(ExternalSourcePull? pull);

  ExternalSource streams(List<ExternalStreamStats>? streams);

  ExternalSource lastValidationErrors(
    List<EdpItemRejection>? lastValidationErrors,
  );

  ExternalSource health(String? health);

  ExternalSource allowPrivateNetwork(bool? allowPrivateNetwork);

  ExternalSource presentation(Map<String, EdpPresentation>? presentation);

  ExternalSource breakingChanges(List<String>? breakingChanges);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExternalSource(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExternalSource(...).copyWith(id: 12, name: "My name")
  /// ```
  ExternalSource call({
    String sourceId,
    ExternalSourceStateEnum state,
    ExternalSourceRegisteredViaEnum? registeredVia,
    EdpManifest manifest,
    String? manifestSha256,
    SourceAccess? access,
    String? egress,
    String? approvedBy,
    DateTime? approvedAt,
    DateTime createdAt,
    DateTime? lastItemAt,
    ExternalSourcePull? pull,
    List<ExternalStreamStats>? streams,
    List<EdpItemRejection>? lastValidationErrors,
    String? health,
    bool? allowPrivateNetwork,
    Map<String, EdpPresentation>? presentation,
    List<String>? breakingChanges,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfExternalSource.copyWith(...)` or call `instanceOfExternalSource.copyWith.fieldName(value)` for a single field.
class _$ExternalSourceCWProxyImpl implements _$ExternalSourceCWProxy {
  const _$ExternalSourceCWProxyImpl(this._value);

  final ExternalSource _value;

  @override
  ExternalSource sourceId(String sourceId) => call(sourceId: sourceId);

  @override
  ExternalSource state(ExternalSourceStateEnum state) => call(state: state);

  @override
  ExternalSource registeredVia(
    ExternalSourceRegisteredViaEnum? registeredVia,
  ) => call(registeredVia: registeredVia);

  @override
  ExternalSource manifest(EdpManifest manifest) => call(manifest: manifest);

  @override
  ExternalSource manifestSha256(String? manifestSha256) =>
      call(manifestSha256: manifestSha256);

  @override
  ExternalSource access(SourceAccess? access) => call(access: access);

  @override
  ExternalSource egress(String? egress) => call(egress: egress);

  @override
  ExternalSource approvedBy(String? approvedBy) => call(approvedBy: approvedBy);

  @override
  ExternalSource approvedAt(DateTime? approvedAt) =>
      call(approvedAt: approvedAt);

  @override
  ExternalSource createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  ExternalSource lastItemAt(DateTime? lastItemAt) =>
      call(lastItemAt: lastItemAt);

  @override
  ExternalSource pull(ExternalSourcePull? pull) => call(pull: pull);

  @override
  ExternalSource streams(List<ExternalStreamStats>? streams) =>
      call(streams: streams);

  @override
  ExternalSource lastValidationErrors(
    List<EdpItemRejection>? lastValidationErrors,
  ) => call(lastValidationErrors: lastValidationErrors);

  @override
  ExternalSource health(String? health) => call(health: health);

  @override
  ExternalSource allowPrivateNetwork(bool? allowPrivateNetwork) =>
      call(allowPrivateNetwork: allowPrivateNetwork);

  @override
  ExternalSource presentation(Map<String, EdpPresentation>? presentation) =>
      call(presentation: presentation);

  @override
  ExternalSource breakingChanges(List<String>? breakingChanges) =>
      call(breakingChanges: breakingChanges);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExternalSource(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExternalSource(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ExternalSource call({
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? state = const $CopyWithPlaceholder(),
    Object? registeredVia = const $CopyWithPlaceholder(),
    Object? manifest = const $CopyWithPlaceholder(),
    Object? manifestSha256 = const $CopyWithPlaceholder(),
    Object? access = const $CopyWithPlaceholder(),
    Object? egress = const $CopyWithPlaceholder(),
    Object? approvedBy = const $CopyWithPlaceholder(),
    Object? approvedAt = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? lastItemAt = const $CopyWithPlaceholder(),
    Object? pull = const $CopyWithPlaceholder(),
    Object? streams = const $CopyWithPlaceholder(),
    Object? lastValidationErrors = const $CopyWithPlaceholder(),
    Object? health = const $CopyWithPlaceholder(),
    Object? allowPrivateNetwork = const $CopyWithPlaceholder(),
    Object? presentation = const $CopyWithPlaceholder(),
    Object? breakingChanges = const $CopyWithPlaceholder(),
  }) {
    return ExternalSource(
      sourceId: sourceId == const $CopyWithPlaceholder() || sourceId == null
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String,
      state: state == const $CopyWithPlaceholder() || state == null
          ? _value.state
          // ignore: cast_nullable_to_non_nullable
          : state as ExternalSourceStateEnum,
      registeredVia: registeredVia == const $CopyWithPlaceholder()
          ? _value.registeredVia
          // ignore: cast_nullable_to_non_nullable
          : registeredVia as ExternalSourceRegisteredViaEnum?,
      manifest: manifest == const $CopyWithPlaceholder() || manifest == null
          ? _value.manifest
          // ignore: cast_nullable_to_non_nullable
          : manifest as EdpManifest,
      manifestSha256: manifestSha256 == const $CopyWithPlaceholder()
          ? _value.manifestSha256
          // ignore: cast_nullable_to_non_nullable
          : manifestSha256 as String?,
      access: access == const $CopyWithPlaceholder()
          ? _value.access
          // ignore: cast_nullable_to_non_nullable
          : access as SourceAccess?,
      egress: egress == const $CopyWithPlaceholder()
          ? _value.egress
          // ignore: cast_nullable_to_non_nullable
          : egress as String?,
      approvedBy: approvedBy == const $CopyWithPlaceholder()
          ? _value.approvedBy
          // ignore: cast_nullable_to_non_nullable
          : approvedBy as String?,
      approvedAt: approvedAt == const $CopyWithPlaceholder()
          ? _value.approvedAt
          // ignore: cast_nullable_to_non_nullable
          : approvedAt as DateTime?,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      lastItemAt: lastItemAt == const $CopyWithPlaceholder()
          ? _value.lastItemAt
          // ignore: cast_nullable_to_non_nullable
          : lastItemAt as DateTime?,
      pull: pull == const $CopyWithPlaceholder()
          ? _value.pull
          // ignore: cast_nullable_to_non_nullable
          : pull as ExternalSourcePull?,
      streams: streams == const $CopyWithPlaceholder()
          ? _value.streams
          // ignore: cast_nullable_to_non_nullable
          : streams as List<ExternalStreamStats>?,
      lastValidationErrors: lastValidationErrors == const $CopyWithPlaceholder()
          ? _value.lastValidationErrors
          // ignore: cast_nullable_to_non_nullable
          : lastValidationErrors as List<EdpItemRejection>?,
      health: health == const $CopyWithPlaceholder()
          ? _value.health
          // ignore: cast_nullable_to_non_nullable
          : health as String?,
      allowPrivateNetwork: allowPrivateNetwork == const $CopyWithPlaceholder()
          ? _value.allowPrivateNetwork
          // ignore: cast_nullable_to_non_nullable
          : allowPrivateNetwork as bool?,
      presentation: presentation == const $CopyWithPlaceholder()
          ? _value.presentation
          // ignore: cast_nullable_to_non_nullable
          : presentation as Map<String, EdpPresentation>?,
      breakingChanges: breakingChanges == const $CopyWithPlaceholder()
          ? _value.breakingChanges
          // ignore: cast_nullable_to_non_nullable
          : breakingChanges as List<String>?,
    );
  }
}

extension $ExternalSourceCopyWith on ExternalSource {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfExternalSource.copyWith(...)` or `instanceOfExternalSource.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ExternalSourceCWProxy get copyWith => _$ExternalSourceCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExternalSource _$ExternalSourceFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ExternalSource', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['sourceId', 'state', 'manifest', 'createdAt'],
  );
  final val = ExternalSource(
    sourceId: $checkedConvert('sourceId', (v) => v as String),
    state: $checkedConvert(
      'state',
      (v) => $enumDecode(_$ExternalSourceStateEnumEnumMap, v),
    ),
    registeredVia: $checkedConvert(
      'registeredVia',
      (v) => $enumDecodeNullable(_$ExternalSourceRegisteredViaEnumEnumMap, v),
    ),
    manifest: $checkedConvert(
      'manifest',
      (v) => EdpManifest.fromJson(v as Map<String, dynamic>),
    ),
    manifestSha256: $checkedConvert('manifestSha256', (v) => v as String?),
    access: $checkedConvert(
      'access',
      (v) =>
          v == null ? null : SourceAccess.fromJson(v as Map<String, dynamic>),
    ),
    egress: $checkedConvert('egress', (v) => v as String?),
    approvedBy: $checkedConvert('approvedBy', (v) => v as String?),
    approvedAt: $checkedConvert(
      'approvedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    lastItemAt: $checkedConvert(
      'lastItemAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    pull: $checkedConvert(
      'pull',
      (v) => v == null
          ? null
          : ExternalSourcePull.fromJson(v as Map<String, dynamic>),
    ),
    streams: $checkedConvert(
      'streams',
      (v) => (v as List<dynamic>?)
          ?.map((e) => ExternalStreamStats.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    lastValidationErrors: $checkedConvert(
      'lastValidationErrors',
      (v) => (v as List<dynamic>?)
          ?.map((e) => EdpItemRejection.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    health: $checkedConvert('health', (v) => v as String?),
    allowPrivateNetwork: $checkedConvert(
      'allowPrivateNetwork',
      (v) => v as bool?,
    ),
    presentation: $checkedConvert(
      'presentation',
      (v) => (v as Map<String, dynamic>?)?.map(
        (k, e) =>
            MapEntry(k, EdpPresentation.fromJson(e as Map<String, dynamic>)),
      ),
    ),
    breakingChanges: $checkedConvert(
      'breakingChanges',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$ExternalSourceToJson(ExternalSource instance) =>
    <String, dynamic>{
      'sourceId': instance.sourceId,
      'state': _$ExternalSourceStateEnumEnumMap[instance.state]!,
      'registeredVia':
          ?_$ExternalSourceRegisteredViaEnumEnumMap[instance.registeredVia],
      'manifest': instance.manifest.toJson(),
      'manifestSha256': ?instance.manifestSha256,
      'access': ?instance.access?.toJson(),
      'egress': ?instance.egress,
      'approvedBy': ?instance.approvedBy,
      'approvedAt': ?instance.approvedAt?.toIso8601String(),
      'createdAt': instance.createdAt.toIso8601String(),
      'lastItemAt': ?instance.lastItemAt?.toIso8601String(),
      'pull': ?instance.pull?.toJson(),
      'streams': ?instance.streams?.map((e) => e.toJson()).toList(),
      'lastValidationErrors': ?instance.lastValidationErrors
          ?.map((e) => e.toJson())
          .toList(),
      'health': ?instance.health,
      'allowPrivateNetwork': ?instance.allowPrivateNetwork,
      'presentation': ?instance.presentation?.map(
        (k, e) => MapEntry(k, e.toJson()),
      ),
      'breakingChanges': ?instance.breakingChanges,
    };

const _$ExternalSourceStateEnumEnumMap = {
  ExternalSourceStateEnum.pending: 'pending',
  ExternalSourceStateEnum.active: 'active',
  ExternalSourceStateEnum.suspended: 'suspended',
};

const _$ExternalSourceRegisteredViaEnumEnumMap = {
  ExternalSourceRegisteredViaEnum.operator_: 'operator',
  ExternalSourceRegisteredViaEnum.registrationToken: 'registration-token',
};
