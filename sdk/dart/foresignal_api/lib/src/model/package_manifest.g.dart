// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'package_manifest.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PackageManifestCWProxy {
  PackageManifest packageId(String packageId);

  PackageManifest type(PackageManifestTypeEnum type);

  PackageManifest objectRef(String objectRef);

  PackageManifest sender(PackageManifestSender sender);

  PackageManifest authors(List<PackageManifestAuthorsInner> authors);

  PackageManifest tlp(Tlp tlp);

  PackageManifest createdAt(DateTime createdAt);

  PackageManifest recipients(Object? recipients);

  PackageManifest contentHashes(Map<String, String> contentHashes);

  PackageManifest message(String? message);

  PackageManifest provenanceChain(List<ProvenanceChainEntry>? provenanceChain);

  PackageManifest title(String? title);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PackageManifest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PackageManifest(...).copyWith(id: 12, name: "My name")
  /// ```
  PackageManifest call({
    String packageId,
    PackageManifestTypeEnum type,
    String objectRef,
    PackageManifestSender sender,
    List<PackageManifestAuthorsInner> authors,
    Tlp tlp,
    DateTime createdAt,
    Object? recipients,
    Map<String, String> contentHashes,
    String? message,
    List<ProvenanceChainEntry>? provenanceChain,
    String? title,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPackageManifest.copyWith(...)` or call `instanceOfPackageManifest.copyWith.fieldName(value)` for a single field.
class _$PackageManifestCWProxyImpl implements _$PackageManifestCWProxy {
  const _$PackageManifestCWProxyImpl(this._value);

  final PackageManifest _value;

  @override
  PackageManifest packageId(String packageId) => call(packageId: packageId);

  @override
  PackageManifest type(PackageManifestTypeEnum type) => call(type: type);

  @override
  PackageManifest objectRef(String objectRef) => call(objectRef: objectRef);

  @override
  PackageManifest sender(PackageManifestSender sender) => call(sender: sender);

  @override
  PackageManifest authors(List<PackageManifestAuthorsInner> authors) =>
      call(authors: authors);

  @override
  PackageManifest tlp(Tlp tlp) => call(tlp: tlp);

  @override
  PackageManifest createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  PackageManifest recipients(Object? recipients) =>
      call(recipients: recipients);

  @override
  PackageManifest contentHashes(Map<String, String> contentHashes) =>
      call(contentHashes: contentHashes);

  @override
  PackageManifest message(String? message) => call(message: message);

  @override
  PackageManifest provenanceChain(
    List<ProvenanceChainEntry>? provenanceChain,
  ) => call(provenanceChain: provenanceChain);

  @override
  PackageManifest title(String? title) => call(title: title);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PackageManifest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PackageManifest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PackageManifest call({
    Object? packageId = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? objectRef = const $CopyWithPlaceholder(),
    Object? sender = const $CopyWithPlaceholder(),
    Object? authors = const $CopyWithPlaceholder(),
    Object? tlp = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? recipients = const $CopyWithPlaceholder(),
    Object? contentHashes = const $CopyWithPlaceholder(),
    Object? message = const $CopyWithPlaceholder(),
    Object? provenanceChain = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
  }) {
    return PackageManifest(
      packageId: packageId == const $CopyWithPlaceholder() || packageId == null
          ? _value.packageId
          // ignore: cast_nullable_to_non_nullable
          : packageId as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as PackageManifestTypeEnum,
      objectRef: objectRef == const $CopyWithPlaceholder() || objectRef == null
          ? _value.objectRef
          // ignore: cast_nullable_to_non_nullable
          : objectRef as String,
      sender: sender == const $CopyWithPlaceholder() || sender == null
          ? _value.sender
          // ignore: cast_nullable_to_non_nullable
          : sender as PackageManifestSender,
      authors: authors == const $CopyWithPlaceholder() || authors == null
          ? _value.authors
          // ignore: cast_nullable_to_non_nullable
          : authors as List<PackageManifestAuthorsInner>,
      tlp: tlp == const $CopyWithPlaceholder() || tlp == null
          ? _value.tlp
          // ignore: cast_nullable_to_non_nullable
          : tlp as Tlp,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      recipients: recipients == const $CopyWithPlaceholder()
          ? _value.recipients
          // ignore: cast_nullable_to_non_nullable
          : recipients as Object?,
      contentHashes:
          contentHashes == const $CopyWithPlaceholder() || contentHashes == null
          ? _value.contentHashes
          // ignore: cast_nullable_to_non_nullable
          : contentHashes as Map<String, String>,
      message: message == const $CopyWithPlaceholder()
          ? _value.message
          // ignore: cast_nullable_to_non_nullable
          : message as String?,
      provenanceChain: provenanceChain == const $CopyWithPlaceholder()
          ? _value.provenanceChain
          // ignore: cast_nullable_to_non_nullable
          : provenanceChain as List<ProvenanceChainEntry>?,
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
    );
  }
}

extension $PackageManifestCopyWith on PackageManifest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPackageManifest.copyWith(...)` or `instanceOfPackageManifest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PackageManifestCWProxy get copyWith => _$PackageManifestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PackageManifest _$PackageManifestFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PackageManifest', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'packageId',
      'type',
      'objectRef',
      'sender',
      'authors',
      'tlp',
      'createdAt',
      'recipients',
      'contentHashes',
    ],
  );
  final val = PackageManifest(
    packageId: $checkedConvert('packageId', (v) => v as String),
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$PackageManifestTypeEnumEnumMap, v),
    ),
    objectRef: $checkedConvert('objectRef', (v) => v as String),
    sender: $checkedConvert(
      'sender',
      (v) => PackageManifestSender.fromJson(v as Map<String, dynamic>),
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
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    recipients: $checkedConvert('recipients', (v) => v),
    contentHashes: $checkedConvert(
      'contentHashes',
      (v) => Map<String, String>.from(v as Map),
    ),
    message: $checkedConvert('message', (v) => v as String?),
    provenanceChain: $checkedConvert(
      'provenanceChain',
      (v) => (v as List<dynamic>?)
          ?.map((e) => ProvenanceChainEntry.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    title: $checkedConvert('title', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$PackageManifestToJson(
  PackageManifest instance,
) => <String, dynamic>{
  'packageId': instance.packageId,
  'type': _$PackageManifestTypeEnumEnumMap[instance.type]!,
  'objectRef': instance.objectRef,
  'sender': instance.sender.toJson(),
  'authors': instance.authors.map((e) => e.toJson()).toList(),
  'tlp': _$TlpEnumMap[instance.tlp]!,
  'createdAt': instance.createdAt.toIso8601String(),
  'recipients': instance.recipients,
  'contentHashes': instance.contentHashes,
  'message': ?instance.message,
  'provenanceChain': ?instance.provenanceChain?.map((e) => e.toJson()).toList(),
  'title': ?instance.title,
};

const _$PackageManifestTypeEnumEnumMap = {
  PackageManifestTypeEnum.report: 'report',
  PackageManifestTypeEnum.investigation: 'investigation',
  PackageManifestTypeEnum.chronicleExtract: 'chronicle-extract',
  PackageManifestTypeEnum.evidence: 'evidence',
};

const _$TlpEnumMap = {
  Tlp.CLEAR: 'CLEAR',
  Tlp.GREEN: 'GREEN',
  Tlp.AMBER: 'AMBER',
  Tlp.aMBERPlusSTRICT: 'AMBER+STRICT',
  Tlp.RED: 'RED',
};
