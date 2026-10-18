// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'received_package.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReceivedPackageCWProxy {
  ReceivedPackage id(String id);

  ReceivedPackage packageId(String packageId);

  ReceivedPackage type(ReceivedPackageTypeEnum type);

  ReceivedPackage objectRef(String objectRef);

  ReceivedPackage title(String title);

  ReceivedPackage sender(ReceivedPackageSender sender);

  ReceivedPackage authors(List<PackageManifestAuthorsInner> authors);

  ReceivedPackage tlp(Tlp tlp);

  ReceivedPackage signatureStatus(
    ReceivedPackageSignatureStatusEnum signatureStatus,
  );

  ReceivedPackage keySource(ReceivedPackageKeySourceEnum? keySource);

  ReceivedPackage hashesVerified(bool hashesVerified);

  ReceivedPackage receivedVia(ReceivedPackageReceivedViaEnum receivedVia);

  ReceivedPackage receivedAt(DateTime receivedAt);

  ReceivedPackage recipients(List<String>? recipients);

  ReceivedPackage message(String? message);

  ReceivedPackage acknowledgedAt(DateTime? acknowledgedAt);

  ReceivedPackage provenanceChain(List<ProvenanceChainEntry>? provenanceChain);

  ReceivedPackage contents(List<ReceivedPackageContentsInner>? contents);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReceivedPackage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReceivedPackage(...).copyWith(id: 12, name: "My name")
  /// ```
  ReceivedPackage call({
    String id,
    String packageId,
    ReceivedPackageTypeEnum type,
    String objectRef,
    String title,
    ReceivedPackageSender sender,
    List<PackageManifestAuthorsInner> authors,
    Tlp tlp,
    ReceivedPackageSignatureStatusEnum signatureStatus,
    ReceivedPackageKeySourceEnum? keySource,
    bool hashesVerified,
    ReceivedPackageReceivedViaEnum receivedVia,
    DateTime receivedAt,
    List<String>? recipients,
    String? message,
    DateTime? acknowledgedAt,
    List<ProvenanceChainEntry>? provenanceChain,
    List<ReceivedPackageContentsInner>? contents,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReceivedPackage.copyWith(...)` or call `instanceOfReceivedPackage.copyWith.fieldName(value)` for a single field.
class _$ReceivedPackageCWProxyImpl implements _$ReceivedPackageCWProxy {
  const _$ReceivedPackageCWProxyImpl(this._value);

  final ReceivedPackage _value;

  @override
  ReceivedPackage id(String id) => call(id: id);

  @override
  ReceivedPackage packageId(String packageId) => call(packageId: packageId);

  @override
  ReceivedPackage type(ReceivedPackageTypeEnum type) => call(type: type);

  @override
  ReceivedPackage objectRef(String objectRef) => call(objectRef: objectRef);

  @override
  ReceivedPackage title(String title) => call(title: title);

  @override
  ReceivedPackage sender(ReceivedPackageSender sender) => call(sender: sender);

  @override
  ReceivedPackage authors(List<PackageManifestAuthorsInner> authors) =>
      call(authors: authors);

  @override
  ReceivedPackage tlp(Tlp tlp) => call(tlp: tlp);

  @override
  ReceivedPackage signatureStatus(
    ReceivedPackageSignatureStatusEnum signatureStatus,
  ) => call(signatureStatus: signatureStatus);

  @override
  ReceivedPackage keySource(ReceivedPackageKeySourceEnum? keySource) =>
      call(keySource: keySource);

  @override
  ReceivedPackage hashesVerified(bool hashesVerified) =>
      call(hashesVerified: hashesVerified);

  @override
  ReceivedPackage receivedVia(ReceivedPackageReceivedViaEnum receivedVia) =>
      call(receivedVia: receivedVia);

  @override
  ReceivedPackage receivedAt(DateTime receivedAt) =>
      call(receivedAt: receivedAt);

  @override
  ReceivedPackage recipients(List<String>? recipients) =>
      call(recipients: recipients);

  @override
  ReceivedPackage message(String? message) => call(message: message);

  @override
  ReceivedPackage acknowledgedAt(DateTime? acknowledgedAt) =>
      call(acknowledgedAt: acknowledgedAt);

  @override
  ReceivedPackage provenanceChain(
    List<ProvenanceChainEntry>? provenanceChain,
  ) => call(provenanceChain: provenanceChain);

  @override
  ReceivedPackage contents(List<ReceivedPackageContentsInner>? contents) =>
      call(contents: contents);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReceivedPackage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReceivedPackage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ReceivedPackage call({
    Object? id = const $CopyWithPlaceholder(),
    Object? packageId = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? objectRef = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? sender = const $CopyWithPlaceholder(),
    Object? authors = const $CopyWithPlaceholder(),
    Object? tlp = const $CopyWithPlaceholder(),
    Object? signatureStatus = const $CopyWithPlaceholder(),
    Object? keySource = const $CopyWithPlaceholder(),
    Object? hashesVerified = const $CopyWithPlaceholder(),
    Object? receivedVia = const $CopyWithPlaceholder(),
    Object? receivedAt = const $CopyWithPlaceholder(),
    Object? recipients = const $CopyWithPlaceholder(),
    Object? message = const $CopyWithPlaceholder(),
    Object? acknowledgedAt = const $CopyWithPlaceholder(),
    Object? provenanceChain = const $CopyWithPlaceholder(),
    Object? contents = const $CopyWithPlaceholder(),
  }) {
    return ReceivedPackage(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      packageId: packageId == const $CopyWithPlaceholder() || packageId == null
          ? _value.packageId
          // ignore: cast_nullable_to_non_nullable
          : packageId as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as ReceivedPackageTypeEnum,
      objectRef: objectRef == const $CopyWithPlaceholder() || objectRef == null
          ? _value.objectRef
          // ignore: cast_nullable_to_non_nullable
          : objectRef as String,
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      sender: sender == const $CopyWithPlaceholder() || sender == null
          ? _value.sender
          // ignore: cast_nullable_to_non_nullable
          : sender as ReceivedPackageSender,
      authors: authors == const $CopyWithPlaceholder() || authors == null
          ? _value.authors
          // ignore: cast_nullable_to_non_nullable
          : authors as List<PackageManifestAuthorsInner>,
      tlp: tlp == const $CopyWithPlaceholder() || tlp == null
          ? _value.tlp
          // ignore: cast_nullable_to_non_nullable
          : tlp as Tlp,
      signatureStatus:
          signatureStatus == const $CopyWithPlaceholder() ||
              signatureStatus == null
          ? _value.signatureStatus
          // ignore: cast_nullable_to_non_nullable
          : signatureStatus as ReceivedPackageSignatureStatusEnum,
      keySource: keySource == const $CopyWithPlaceholder()
          ? _value.keySource
          // ignore: cast_nullable_to_non_nullable
          : keySource as ReceivedPackageKeySourceEnum?,
      hashesVerified:
          hashesVerified == const $CopyWithPlaceholder() ||
              hashesVerified == null
          ? _value.hashesVerified
          // ignore: cast_nullable_to_non_nullable
          : hashesVerified as bool,
      receivedVia:
          receivedVia == const $CopyWithPlaceholder() || receivedVia == null
          ? _value.receivedVia
          // ignore: cast_nullable_to_non_nullable
          : receivedVia as ReceivedPackageReceivedViaEnum,
      receivedAt:
          receivedAt == const $CopyWithPlaceholder() || receivedAt == null
          ? _value.receivedAt
          // ignore: cast_nullable_to_non_nullable
          : receivedAt as DateTime,
      recipients: recipients == const $CopyWithPlaceholder()
          ? _value.recipients
          // ignore: cast_nullable_to_non_nullable
          : recipients as List<String>?,
      message: message == const $CopyWithPlaceholder()
          ? _value.message
          // ignore: cast_nullable_to_non_nullable
          : message as String?,
      acknowledgedAt: acknowledgedAt == const $CopyWithPlaceholder()
          ? _value.acknowledgedAt
          // ignore: cast_nullable_to_non_nullable
          : acknowledgedAt as DateTime?,
      provenanceChain: provenanceChain == const $CopyWithPlaceholder()
          ? _value.provenanceChain
          // ignore: cast_nullable_to_non_nullable
          : provenanceChain as List<ProvenanceChainEntry>?,
      contents: contents == const $CopyWithPlaceholder()
          ? _value.contents
          // ignore: cast_nullable_to_non_nullable
          : contents as List<ReceivedPackageContentsInner>?,
    );
  }
}

extension $ReceivedPackageCopyWith on ReceivedPackage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReceivedPackage.copyWith(...)` or `instanceOfReceivedPackage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReceivedPackageCWProxy get copyWith => _$ReceivedPackageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReceivedPackage _$ReceivedPackageFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ReceivedPackage', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'packageId',
      'type',
      'objectRef',
      'title',
      'sender',
      'authors',
      'tlp',
      'signatureStatus',
      'hashesVerified',
      'receivedVia',
      'receivedAt',
    ],
  );
  final val = ReceivedPackage(
    id: $checkedConvert('id', (v) => v as String),
    packageId: $checkedConvert('packageId', (v) => v as String),
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$ReceivedPackageTypeEnumEnumMap, v),
    ),
    objectRef: $checkedConvert('objectRef', (v) => v as String),
    title: $checkedConvert('title', (v) => v as String),
    sender: $checkedConvert(
      'sender',
      (v) => ReceivedPackageSender.fromJson(v as Map<String, dynamic>),
    ),
    authors: $checkedConvert(
      'authors',
      (v) => (v as List<dynamic>)
          .map(
            (e) =>
                PackageManifestAuthorsInner.fromJson(e as Map<String, dynamic>),
          )
          .toList(),
    ),
    tlp: $checkedConvert('tlp', (v) => $enumDecode(_$TlpEnumMap, v)),
    signatureStatus: $checkedConvert(
      'signatureStatus',
      (v) => $enumDecode(_$ReceivedPackageSignatureStatusEnumEnumMap, v),
    ),
    keySource: $checkedConvert(
      'keySource',
      (v) => $enumDecodeNullable(_$ReceivedPackageKeySourceEnumEnumMap, v),
    ),
    hashesVerified: $checkedConvert('hashesVerified', (v) => v as bool),
    receivedVia: $checkedConvert(
      'receivedVia',
      (v) => $enumDecode(_$ReceivedPackageReceivedViaEnumEnumMap, v),
    ),
    receivedAt: $checkedConvert(
      'receivedAt',
      (v) => DateTime.parse(v as String),
    ),
    recipients: $checkedConvert(
      'recipients',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    message: $checkedConvert('message', (v) => v as String?),
    acknowledgedAt: $checkedConvert(
      'acknowledgedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    provenanceChain: $checkedConvert(
      'provenanceChain',
      (v) => (v as List<dynamic>?)
          ?.map((e) => ProvenanceChainEntry.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    contents: $checkedConvert(
      'contents',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => ReceivedPackageContentsInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$ReceivedPackageToJson(
  ReceivedPackage instance,
) => <String, dynamic>{
  'id': instance.id,
  'packageId': instance.packageId,
  'type': _$ReceivedPackageTypeEnumEnumMap[instance.type]!,
  'objectRef': instance.objectRef,
  'title': instance.title,
  'sender': instance.sender.toJson(),
  'authors': instance.authors.map((e) => e.toJson()).toList(),
  'tlp': _$TlpEnumMap[instance.tlp]!,
  'signatureStatus':
      _$ReceivedPackageSignatureStatusEnumEnumMap[instance.signatureStatus]!,
  'keySource': ?_$ReceivedPackageKeySourceEnumEnumMap[instance.keySource],
  'hashesVerified': instance.hashesVerified,
  'receivedVia': _$ReceivedPackageReceivedViaEnumEnumMap[instance.receivedVia]!,
  'receivedAt': instance.receivedAt.toIso8601String(),
  'recipients': ?instance.recipients,
  'message': ?instance.message,
  'acknowledgedAt': ?instance.acknowledgedAt?.toIso8601String(),
  'provenanceChain': ?instance.provenanceChain?.map((e) => e.toJson()).toList(),
  'contents': ?instance.contents?.map((e) => e.toJson()).toList(),
};

const _$ReceivedPackageTypeEnumEnumMap = {
  ReceivedPackageTypeEnum.report: 'report',
  ReceivedPackageTypeEnum.investigation: 'investigation',
  ReceivedPackageTypeEnum.chronicleExtract: 'chronicle-extract',
  ReceivedPackageTypeEnum.evidence: 'evidence',
};

const _$TlpEnumMap = {
  Tlp.CLEAR: 'CLEAR',
  Tlp.GREEN: 'GREEN',
  Tlp.AMBER: 'AMBER',
  Tlp.aMBERPlusSTRICT: 'AMBER+STRICT',
  Tlp.RED: 'RED',
};

const _$ReceivedPackageSignatureStatusEnumEnumMap = {
  ReceivedPackageSignatureStatusEnum.valid: 'valid',
  ReceivedPackageSignatureStatusEnum.invalid: 'invalid',
  ReceivedPackageSignatureStatusEnum.unknownKey: 'unknown-key',
};

const _$ReceivedPackageKeySourceEnumEnumMap = {
  ReceivedPackageKeySourceEnum.peerJwks: 'peer-jwks',
  ReceivedPackageKeySourceEnum.pinned: 'pinned',
};

const _$ReceivedPackageReceivedViaEnumEnumMap = {
  ReceivedPackageReceivedViaEnum.peer: 'peer',
  ReceivedPackageReceivedViaEnum.import_: 'import',
};
