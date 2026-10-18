//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'forecast_citations_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ForecastCitationsInner {
  /// Returns a new [ForecastCitationsInner] instance.
  ForecastCitationsInner({

    required  this.ref,

     this.quote,

     this.claim,
  });

      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'ref',
    required: true,
    includeIfNull: false,
  )


  final String ref;



  @JsonKey(
    
    name: r'quote',
    required: false,
    includeIfNull: false,
  )


  final String? quote;



  @JsonKey(
    
    name: r'claim',
    required: false,
    includeIfNull: false,
  )


  final String? claim;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ForecastCitationsInner &&
      other.ref == ref &&
      other.quote == quote &&
      other.claim == claim;

    @override
    int get hashCode =>
        ref.hashCode +
        (quote == null ? 0 : quote.hashCode) +
        (claim == null ? 0 : claim.hashCode);

  factory ForecastCitationsInner.fromJson(Map<String, dynamic> json) => _$ForecastCitationsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ForecastCitationsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

