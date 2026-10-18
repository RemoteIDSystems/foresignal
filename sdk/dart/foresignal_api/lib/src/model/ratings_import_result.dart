//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/ratings_import_result_errors_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ratings_import_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RatingsImportResult {
  /// Returns a new [RatingsImportResult] instance.
  RatingsImportResult({

    required  this.provider,

    required  this.imported,

    required  this.updated,

    required  this.rejected,

     this.outletsCreated,

     this.errors,

    required  this.importedAt,
  });

  @JsonKey(
    
    name: r'provider',
    required: true,
    includeIfNull: false,
  )


  final RatingsImportResultProviderEnum provider;



  @JsonKey(
    
    name: r'imported',
    required: true,
    includeIfNull: false,
  )


  final int imported;



  @JsonKey(
    
    name: r'updated',
    required: true,
    includeIfNull: false,
  )


  final int updated;



  @JsonKey(
    
    name: r'rejected',
    required: true,
    includeIfNull: false,
  )


  final int rejected;



  @JsonKey(
    
    name: r'outletsCreated',
    required: false,
    includeIfNull: false,
  )


  final int? outletsCreated;



  @JsonKey(
    
    name: r'errors',
    required: false,
    includeIfNull: false,
  )


  final List<RatingsImportResultErrorsInner>? errors;



  @JsonKey(
    
    name: r'importedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime importedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RatingsImportResult &&
      other.provider == provider &&
      other.imported == imported &&
      other.updated == updated &&
      other.rejected == rejected &&
      other.outletsCreated == outletsCreated &&
      other.errors == errors &&
      other.importedAt == importedAt;

    @override
    int get hashCode =>
        provider.hashCode +
        imported.hashCode +
        updated.hashCode +
        rejected.hashCode +
        outletsCreated.hashCode +
        errors.hashCode +
        importedAt.hashCode;

  factory RatingsImportResult.fromJson(Map<String, dynamic> json) => _$RatingsImportResultFromJson(json);

  Map<String, dynamic> toJson() => _$RatingsImportResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum RatingsImportResultProviderEnum {
@JsonValue(r'wikidata')
wikidata(r'wikidata'),
@JsonValue(r'partisan-web')
partisanWeb(r'partisan-web'),
@JsonValue(r'allsides')
allsides(r'allsides'),
@JsonValue(r'mbfc')
mbfc(r'mbfc'),
@JsonValue(r'operator')
operator_(r'operator');

const RatingsImportResultProviderEnum(this.value);

final String value;

@override
String toString() => value;
}


