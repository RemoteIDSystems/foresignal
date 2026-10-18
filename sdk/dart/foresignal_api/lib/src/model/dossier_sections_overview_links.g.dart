// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'dossier_sections_overview_links.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DossierSectionsOverviewLinksCWProxy {
  DossierSectionsOverviewLinks full(String? full);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DossierSectionsOverviewLinks(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DossierSectionsOverviewLinks(...).copyWith(id: 12, name: "My name")
  /// ```
  DossierSectionsOverviewLinks call({String? full});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDossierSectionsOverviewLinks.copyWith(...)` or call `instanceOfDossierSectionsOverviewLinks.copyWith.fieldName(value)` for a single field.
class _$DossierSectionsOverviewLinksCWProxyImpl
    implements _$DossierSectionsOverviewLinksCWProxy {
  const _$DossierSectionsOverviewLinksCWProxyImpl(this._value);

  final DossierSectionsOverviewLinks _value;

  @override
  DossierSectionsOverviewLinks full(String? full) => call(full: full);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DossierSectionsOverviewLinks(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DossierSectionsOverviewLinks(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DossierSectionsOverviewLinks call({
    Object? full = const $CopyWithPlaceholder(),
  }) {
    return DossierSectionsOverviewLinks(
      full: full == const $CopyWithPlaceholder()
          ? _value.full
          // ignore: cast_nullable_to_non_nullable
          : full as String?,
    );
  }
}

extension $DossierSectionsOverviewLinksCopyWith
    on DossierSectionsOverviewLinks {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDossierSectionsOverviewLinks.copyWith(...)` or `instanceOfDossierSectionsOverviewLinks.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DossierSectionsOverviewLinksCWProxy get copyWith =>
      _$DossierSectionsOverviewLinksCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DossierSectionsOverviewLinks _$DossierSectionsOverviewLinksFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('DossierSectionsOverviewLinks', json, ($checkedConvert) {
  final val = DossierSectionsOverviewLinks(
    full: $checkedConvert('full', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$DossierSectionsOverviewLinksToJson(
  DossierSectionsOverviewLinks instance,
) => <String, dynamic>{'full': ?instance.full};
